package com.citycab.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConnectionRegistry {
    
    // Session ID -> Connection Info
    private final ConcurrentMap<String, ConnectionInfo> connections = new ConcurrentHashMap<>();
    
    // User ID -> List of Session IDs
    private final ConcurrentMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // City ID -> List of User IDs
    private final ConcurrentMap<String, Set<String>> cityUsers = new ConcurrentHashMap<>();
    
    // Subscription tracking
    private final ConcurrentMap<String, Set<String>> sessionSubscriptions = 
        new ConcurrentHashMap<>();
    
    public void registerConnection(String sessionId, String userId, String userType,
                                  String cityId, String countryCode, String connectionType) {
        ConnectionInfo info = ConnectionInfo.builder()
            .sessionId(sessionId)
            .userId(userId)
            .userType(userType)
            .cityId(cityId)
            .countryCode(countryCode)
            .connectionType(connectionType)
            .connectedAt(new Date())
            .lastActivity(new Date())
            .isActive(true)
            .build();
        
        connections.put(sessionId, info);
        
        // Mettre à jour userSessions
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
            .add(sessionId);
        
        // Mettre à jour cityUsers
        if (cityId != null) {
            cityUsers.computeIfAbsent(cityId, k -> ConcurrentHashMap.newKeySet())
                .add(userId);
        }
        
        log.debug("Registered connection: {}", info);
    }
    
    public void addSubscription(String sessionId, String userId, String destination) {
        sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
            .add(destination);
        
        // Mettre à jour last activity
        ConnectionInfo info = connections.get(sessionId);
        if (info != null) {
            info.setLastActivity(new Date());
        }
    }
    
    public void removeConnection(String sessionId) {
        ConnectionInfo info = connections.remove(sessionId);
        if (info != null) {
            // Retirer de userSessions
            Set<String> sessions = userSessions.get(info.getUserId());
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(info.getUserId());
                }
            }
            
            // Retirer de cityUsers
            if (info.getCityId() != null) {
                Set<String> users = cityUsers.get(info.getCityId());
                if (users != null) {
                    users.remove(info.getUserId());
                    if (users.isEmpty()) {
                        cityUsers.remove(info.getCityId());
                    }
                }
            }
            
            // Nettoyer les subscriptions
            sessionSubscriptions.remove(sessionId);
            
            info.setActive(false);
            info.setDisconnectedAt(new Date());
            
            log.debug("Removed connection: {}", info);
        }
    }
    
    public ConnectionInfo getConnection(String sessionId) {
        return connections.get(sessionId);
    }
    
    public Set<String> getUserSessions(String userId) {
        return userSessions.getOrDefault(userId, Collections.emptySet());
    }
    
    public Set<String> getUsersInCity(String cityId) {
        return cityUsers.getOrDefault(cityId, Collections.emptySet());
    }
    
    public Set<String> getSessionsSubscribedTo(String destination) {
        return sessionSubscriptions.entrySet().stream()
            .filter(entry -> entry.getValue().contains(destination))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    public List<ConnectionInfo> getActiveConnections() {
        return connections.values().stream()
            .filter(ConnectionInfo::isActive)
            .collect(Collectors.toList());
    }
    
    public List<ConnectionInfo> getActiveDriversInCity(String cityId) {
        return connections.values().stream()
            .filter(ConnectionInfo::isActive)
            .filter(conn -> "DRIVER".equals(conn.getUserType()))
            .filter(conn -> cityId.equals(conn.getCityId()))
            .collect(Collectors.toList());
    }
    
    public void updateLastActivity(String sessionId) {
        ConnectionInfo info = connections.get(sessionId);
        if (info != null) {
            info.setLastActivity(new Date());
        }
    }
    
    public void cleanupInactiveConnections(long timeoutMs) {
        long cutoff = System.currentTimeMillis() - timeoutMs;
        
        connections.values().stream()
            .filter(ConnectionInfo::isActive)
            .filter(conn -> conn.getLastActivity().getTime() < cutoff)
            .forEach(conn -> {
                log.warn("Cleaning up inactive connection: {}", conn.getSessionId());
                removeConnection(conn.getSessionId());
            });
    }
    
    @Data
    @Builder
    public static class ConnectionInfo {
        private String sessionId;
        private String userId;
        private String userType;
        private String cityId;
        private String countryCode;
        private String connectionType;
        private Date connectedAt;
        private Date lastActivity;
        private Date disconnectedAt;
        private boolean isActive;
    }
}