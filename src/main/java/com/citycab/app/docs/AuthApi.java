package com.citycab.app.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.citycab.app.dtos.AuthResponse;
import com.citycab.app.dtos.JwtClaims;
import com.citycab.app.dtos.LoginRequest;
import com.citycab.app.dtos.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API d'authentification et gestion des tokens")
public interface AuthApi {
    

    @Operation(summary = "Connexion utilisateur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @ApiResponse(responseCode = "403", description = "Compte désactivé")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);
    
    @Operation(summary = "Rafraîchir le token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token rafraîchi"),
        @ApiResponse(responseCode = "401", description = "Refresh token invalide")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    )throws IOException;
    
    @Operation(summary = "Déconnexion")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Déconnexion réussie")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    );
    
    @Operation(summary = "Valider un token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token valide"),
        @ApiResponse(responseCode = "401", description = "Token invalide")
    })
    @GetMapping("/validate")
    public ResponseEntity<JwtClaims> validateToken(@RequestParam String token);
}
