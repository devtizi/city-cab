package com.citycab.app.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    
    // Méthodes à implémenter dans UserEntity
    Set<Role> getRoles();
    
    // Méthode pour obtenir le rôle principal (premier rôle)
    default String getPrimaryRole() {
        Set<Role> userRoles = getRoles();
        if (userRoles != null && !userRoles.isEmpty()) {
            return userRoles.iterator().next().getCode(); // Retourne le code du premier rôle
        }
        return "USER"; // Rôle par défaut
    }

    // Methode par defaut
    default Set<String> getAuthorityStrings() {
        return getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    default Set<String> getPermissions() {
        Set<String> permissions = new HashSet<>();
        
        Set<Role> userRoles = getRoles();
        if (userRoles != null) {
            for (Role role : userRoles) {
                if (role.getPermissions() != null) {
                    permissions.addAll(role.getPermissions());
                }
            }
        }
        
        return permissions;
    }

    default Set<String> getRoleCodes() {
        Set<String> roleCodes = new HashSet<>();

        Set<Role> userRoles = getRoles();
        if (userRoles != null) {
            userRoles.stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .filter(code -> !code.isBlank())
                .forEach(roleCodes::add);
        }
        
        return roleCodes;
    }

    default boolean hasAuthority(String authority) {
        if (authority == null || authority.isBlank()) return false;
        
        return getAuthorities().stream()
            .anyMatch(auth -> authority.equals(auth.getAuthority()));
    }

    default boolean hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return false;
        
        Set<Role> userRoles = getRoles();
        if (userRoles != null) {
            return userRoles.stream().anyMatch(role -> roleCode.equalsIgnoreCase(role.getCode()));
        }
        
        return false;
    }

    // Vérifie si l'utilisateur a un rôle avec des permissions spécifiques
    default boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) return false;
        
        Set<Role> userRoles = getRoles();
        if (userRoles != null) {
            return userRoles.stream()
                .anyMatch(role -> role.getPermissions() != null && 
                    role.getPermissions().contains(permission));
        }
        
        return false;
    }
}