package com.citycab.app.config;


import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "citycab.security")
public class SecurityProperties {
    private Boolean enabled;
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String jwkSetUri;
    private String issuer;
    private int tokenValidationCacheTtl = 300;
    // To set the default security filter chain to permitAll
    private boolean disableAutoSecurity = false;
    private List<String> publicPaths = Arrays.asList(
        "/public/**", 
        "/health/**", 
        "/actuator/**",
        "/error",

        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-ui/index.html",

        "/api/v1/auth/**"
    );
}