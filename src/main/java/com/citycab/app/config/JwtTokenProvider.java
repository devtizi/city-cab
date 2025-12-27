package com.citycab.app.config;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.citycab.app.dtos.JwtClaims;
import com.citycab.app.enums.UserRole;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    //@Value("${jwt.issuer}")
    //private String issuer;
    
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    
    @Value("${jwt.audience.web}")
    private String webAudience;
    
    @Value("${jwt.audience.mobile}")
    private String mobileAudience;
    
    public String generateAccessToken(JwtClaims claims) {
        Instant now = Instant.now();

        List<String> audiences = new ArrayList<>();
        audiences.add(webAudience);
        audiences.add(mobileAudience);
        
        JwtClaimsSet claimsSet = 
            JwtClaimsSet.builder()
                .issuer(getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiration, ChronoUnit.MINUTES))
                .subject(claims.getUsername())
                .claim("userId", claims.getUserId())
                .claim("username", claims.getUsername())
                .claim("roles", claims.getRoles())
                .claim("authorities", claims.getAuthorities())
                .claim("countryCode", claims.getCountryCode())
                .claim("cityId", claims.getCityId())
                .claim("city", claims.getCityId())
                .audience(audiences)
                .build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public String generateRefreshToken(String userId) {
        Instant now = Instant.now();
        
        org.springframework.security.oauth2.jwt.JwtClaimsSet claimsSet = 
            org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer(getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenExpiration, ChronoUnit.DAYS))
                .subject(userId)
                .claim("tokenType", "REFRESH")
                .build();
        
        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
    
    public JwtClaims parseToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            
            return JwtClaims.builder()
                .userId(jwt.getClaim("userId"))
                .username(jwt.getClaim("username"))
                .roles(jwt.getClaim("roles"))
                .authorities(jwt.getClaim("authorities"))
                .iss(jwt.getIssuer().toString())
                .iat(jwt.getIssuedAt().getEpochSecond())
                .exp(jwt.getExpiresAt().getEpochSecond())
                .aud(jwt.getAudience().toString())
                .countryCode(jwt.getClaim("countryCode"))
                .cityId(jwt.getClaim("cityId"))
                .build();
        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
            throw new RuntimeException("Invalid JWT token");
        }
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getUsername();
    }

    
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private  String getIssuer() {
        HttpServletRequest request = getCurrentHttpRequest();
        return request != null ? request.getRequestURL().toString() : "citycab-app";
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}