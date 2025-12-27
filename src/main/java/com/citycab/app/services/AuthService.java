package com.citycab.app.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.citycab.app.config.JwtTokenProvider;
import com.citycab.app.config.LogoutService;
import com.citycab.app.dtos.AuthResponse;
import com.citycab.app.dtos.JwtClaims;
import com.citycab.app.dtos.LoginRequest;
import com.citycab.app.dtos.TokenResponse;
import com.citycab.app.dtos.UserResponse;
import com.citycab.app.entities.Role;
import com.citycab.app.entities.TokenEntity;
import com.citycab.app.entities.UserEntity;
import com.citycab.app.enums.SocialNetworkAuthProvider;
import com.citycab.app.enums.TokenType;
import com.citycab.app.repository.TokenRepository;
import com.citycab.app.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final LogoutService logoutService;

    public AuthResponse register(LoginRequest request) {
        UserEntity user = UserEntity.builder()
            .email(request.getEmail())
            .identifier(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .createdAt(LocalDateTime.now())
            .provider(SocialNetworkAuthProvider.EMAIL)
            .updatedAt(LocalDateTime.now())
            .build();
        UserEntity savedUser = userRepository.save(user);
        return generateAuthResponse(savedUser);
    }

    public UserEntity buildUserWithRole(String email, String password, String roleCode) {

        return UserEntity.builder()
            .email(email)
            .identifier(email)
            .password(passwordEncoder.encode(password))
            .createdAt(LocalDateTime.now())
            .provider(SocialNetworkAuthProvider.EMAIL)
            .updatedAt(LocalDateTime.now())
            .build();
    }

    public TokenResponse refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    )throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
        throw new SecurityException("Missing or invalid Authorization header");   
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtTokenProvider.getUsernameFromToken(refreshToken);
        System.out.println(userEmail);
        if (userEmail == null || SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new SecurityException("Unauthorized access");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new SecurityException("Invalid or expired token");
        }

        UserEntity user = this.userRepository.findByEmail(userEmail).orElseThrow();
        String accessToken = jwtTokenProvider.generateAccessToken((buildJwtClaims(user)));
        revokeUserTokens(user);
        saveUserToken(user, accessToken);
        TokenResponse authResponse = TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
        return authResponse;
    }

    public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        logoutService.logout(request, response, authentication);
    }

    public JwtClaims validateToken(String token){
        return jwtTokenProvider.parseToken(token);
    }

    private AuthResponse generateAuthResponse(UserEntity user) {
        var jwtToken = jwtTokenProvider.generateAccessToken(buildJwtClaims(user));
        var refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        saveUserToken(user, jwtToken);
        return buildAuthResponse(user, jwtToken, refreshToken);
    }

    private AuthResponse buildAuthResponse(UserEntity user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
        .user(UserResponse.fromEntity(user))
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
    }

    private void saveUserToken(UserEntity user, String jwtToken) {
        TokenEntity token = TokenEntity.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
        tokenRepository.save(token);
    }

    private void revokeUserTokens(UserEntity user) {
        List<TokenEntity> userTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (userTokens.isEmpty())
        return;
        userTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(userTokens);
    }

    private JwtClaims buildJwtClaims(UserEntity user) {
        JwtClaims token = JwtClaims.builder()
            .userId(user.getId())
            .username(user.getIdentifier())
            .roles(user.getRoleCodes())
            .countryCode(user.getCountryCode())
            .cityId(user.getCityId())
            .city(user.getCity())
            .userType(user.getUserType().name())
            .authorities(user.getAuthorityStrings())
            .build();
        
        return token;
    }
}
