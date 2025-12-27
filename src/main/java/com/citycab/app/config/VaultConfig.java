package com.citycab.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import lombok.Data;
/*
@Data
@Configuration
@VaultPropertySource(
    value = "secret/citycab/config",
    propertyNamePrefix = "vault.",
    renewal = VaultPropertySource.Renewal.ROTATE,
    ignoreSecretNotFound = true  // Ajoutez ceci
)
public class VaultConfig {

    @Value("${db.password}")
    private String dbPassword;
    
    @Value("${api.key}")
    private String apiKey;
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint endpoint = VaultEndpoint.create("vault", 8200);
        endpoint.setScheme("http");
        
        TokenAuthentication auth = new TokenAuthentication("root");
        VaultTemplate template = new VaultTemplate(endpoint, auth);

        return template;
    }
}
*/