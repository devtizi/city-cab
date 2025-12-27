package com.citycab.app.dtos;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private String userId;
    private String username;
    @Builder.Default
    private Set<String> authorities = new HashSet<>();

    @Builder.Default
    private Set<String> roles = new HashSet<>();
    private String userType;
    private String iss;
    private Long iat;
    private Long exp;
    private String aud;
    private String countryCode;
    private String city;
    private String cityId;
}