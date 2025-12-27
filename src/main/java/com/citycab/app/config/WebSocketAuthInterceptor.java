package com.citycab.app.config;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.citycab.app.dtos.JwtClaims;
import com.citycab.app.enums.UserRole;
import com.citycab.app.exception.InvalidTokenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final ConnectionRegistry connectionRegistry;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
            message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }
        
        StompCommand command = accessor.getCommand();
        
        if (null != command) // Gérer les différentes commandes STOMP
        
        switch (command) {
            case CONNECT:
                handleConnect(accessor);
                break;
            case SUBSCRIBE:
                handleSubscribe(accessor);
                break;
            case DISCONNECT:
                handleDisconnect(accessor);
                break;
            case SEND:
                validateSendMessage(accessor);
                break;
            default:
                break;
        }
        
        return message;
    }
    
    private void handleConnect(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        
        if (token == null) {
            log.warn("No token provided in CONNECT");
            throw new InvalidTokenException("Authentication token is required");
        }
        
        try {
            // Valider le token JWT
            JwtClaims claims = jwtTokenProvider.parseToken(token);
            
            // Créer l'authentification
            Authentication auth = createAuthentication(claims);
            accessor.setUser(auth);
            
            // Enregistrer la connexion
            String sessionId = accessor.getSessionId();
            String userId = claims.getUserId();
            String userType = claims.getUserType();
            String cityId = claims.getCityId();
            String countryCode = claims.getCountryCode();
            
            connectionRegistry.registerConnection(
                sessionId, 
                userId, 
                userType,
                cityId,
                countryCode,
                getConnectionType(accessor)
            );
            
            log.info("User {} connected via WebSocket. Session: {}, City: {}", 
                userId, sessionId, cityId);
            
        } catch (Exception e) {
            log.error("WebSocket authentication failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid authentication token");
        }
    }
    
    private void handleSubscribe(StompHeaderAccessor accessor) {

        if (accessor == null) {
            throw new InvalidTokenException("Authentication required for subscription");
        }
        
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        
        // Valider les permissions de subscription
        validateSubscription(accessor, destination);
        
        // Enregistrer la subscription
        String sessionId = accessor.getSessionId();
        Authentication auth = getAuthentication(accessor);
        String userId = auth.getName();

        connectionRegistry.addSubscription(sessionId, userId, destination);
        
        log.debug("User {} subscribed to {}", userId, destination);
    }
    
    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        if (sessionId != null) {
            connectionRegistry.removeConnection(sessionId);
            log.info("WebSocket session disconnected: {}", sessionId);
        }
    }
    
    private void validateSendMessage(StompHeaderAccessor accessor) {
        
        if (accessor == null) {
            throw new InvalidTokenException("Authentication required for sending messages");
        }
        
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new IllegalArgumentException("Destination is required");
        }
        
        // Valider les permissions d'envoi
        validateSendPermission(accessor, destination);
    }
    
    private String extractToken(StompHeaderAccessor accessor) {
        // Chercher le token dans les headers
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            return authHeader;
        }
        
        // Chercher dans les query parameters (pour SockJS)
        List<String> cookieHeaders = accessor.getNativeHeader("cookie");

        if (cookieHeaders != null) {
            for (String cookie : cookieHeaders) {
                // parser le cookie si besoin
            }
        }

        
        return null;
    }
    
    private Authentication createAuthentication(JwtClaims claims) {
        String username = claims.getUsername();
        String userId = claims.getUserId();
        
        // Convertir les authorities du JWT en SimpleGrantedAuthority
        Set<SimpleGrantedAuthority> authorities = claims.getAuthorities().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
        
        // Ajouter le rôle comme authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.getUserType()));
        
        // Créer un token d'authentification avec les claims
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(userId, null, authorities);
        
        // Stocker les claims supplémentaires
        authToken.setDetails(claims);
        
        return authToken;
    }
    
    private void validateSubscription(StompHeaderAccessor accessor, String destination) {
        
        Authentication auth = getAuthentication(accessor);
        String userId = auth.getName();
        JwtClaims claims = (JwtClaims) auth.getDetails();

        // Règles de validation par destination
        if (destination.startsWith("/topic/driver/")) {
            // Seul le chauffeur peut s'abonner à ses propres topics
            String driverId = extractIdFromDestination(destination, "/topic/driver/");
            if (!driverId.equals(userId) && !isAdmin(claims)) {
                throw new SecurityException("Access denied to driver topic");
            }
        } else if (destination.startsWith("/topic/city/")) {
            // Vérifier que l'utilisateur est dans la bonne ville
            String cityId = extractIdFromDestination(destination, "/topic/city/");
            if (!cityId.equals(claims.getCityId()) && !isAdmin(claims)) {
                throw new SecurityException("Access denied to city topic");
            }
        } else if (destination.startsWith("/user/queue/")) {
            // Les queues personnelles sont autorisées
            // Vérifier le format
            if (!destination.matches("/user/queue/[a-zA-Z0-9-]+")) {
                throw new IllegalArgumentException("Invalid queue destination");
            }
        }
    }
    
    private void validateSendPermission(StompHeaderAccessor accessor, String destination) {
        
        Authentication auth = getAuthentication(accessor);
        String userId = auth.getName();
        JwtClaims claims = (JwtClaims) auth.getDetails();
        
        if (destination.startsWith("/app/driver/")) {
            // Seuls les chauffeurs peuvent envoyer des messages driver
            if (!claims.getUserType().equals(UserRole.DRIVER)) {
                throw new SecurityException("Only drivers can send driver messages");
            }
        } else if (destination.startsWith("/app/ride/")) {
            // Les rides peuvent être créés par tous les utilisateurs
            // Vérification supplémentaire selon le type d'utilisateur
        }
    }
    
    private boolean isAdmin(JwtClaims claims) {
        return claims.getAuthorities().stream()
            .anyMatch(auth -> auth.contains("ADMIN") || auth.contains("ROOTADMIN"));
    }
    
    private String extractIdFromDestination(String destination, String prefix) {
        return destination.substring(prefix.length());
    }
    
    private String getConnectionType(StompHeaderAccessor accessor) {
        String userAgent = accessor.getFirstNativeHeader("User-Agent");
        if (userAgent != null) {
            if (userAgent.contains("Android") || userAgent.contains("iOS")) {
                return "MOBILE";
            } else if (userAgent.contains("Postman") || userAgent.contains("curl")) {
                return "TEST";
            }
        }
        return "WEB";
    }
    
    @Override
    public void afterSendCompletion(
        Message<?> message, 
        MessageChannel channel, 
        boolean sent, 
        Exception ex
    ) {
        if (ex != null) {
            log.error("Error sending WebSocket message: {}", ex.getMessage());
        }
    }

    private Authentication getAuthentication(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof Authentication authentication) {
            return authentication;
        }
        throw new InvalidTokenException("Invalid authentication context");
    }

}