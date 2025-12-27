package com.citycab.app.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.citycab.app.docs.AuthApi;
import com.citycab.app.dtos.AuthResponse;
import com.citycab.app.dtos.JwtClaims;
import com.citycab.app.dtos.LoginRequest;
import com.citycab.app.dtos.TokenResponse;
import com.citycab.app.services.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API d'authentification et gestion des tokens")
public class AuthController implements AuthApi {
    
    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }
    
    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        authService.logout(request, response, authentication);
        return ResponseEntity.ok().build();
    }
    
    @Override
    @GetMapping("/validate")
    public ResponseEntity<JwtClaims> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateToken(token));
    }   
}
