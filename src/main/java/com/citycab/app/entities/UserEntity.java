package com.citycab.app.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.citycab.app.enums.SocialNetworkAuthProvider;
import com.citycab.app.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)  
public class UserEntity extends AuditableEntity implements CustomUserDetails{
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true)
    private String phone;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String identifier;

    private String profileImage;
    private String coverImage;
    private String fcmToken;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SocialNetworkAuthProvider provider = SocialNetworkAuthProvider.EMAIL;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Column(nullable = false, length = 20, name = "user_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole userType = UserRole.USER;

    private String countryCode;
    private String city;
    private String cityId;
    
    public String getDisplayName() {
        if (this.firstName != null && !this.firstName.isBlank() && this.lastName != null && !this.lastName.isBlank()) {
        return this.firstName + " " + this.lastName;
        } else if (this.firstName != null && !this.firstName.isBlank()) {
        return this.firstName;
        } else if (this.lastName != null && !this.lastName.isBlank()) {
        return this.lastName;
        } else if (this.email != null && !this.email.isBlank() && this.email.contains("@")) {
        return this.email.substring(0, this.email.indexOf("@"));
        }
        return "";
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        
        // 2. Ajouter les rôles de la table roles
        if (this.roles != null) {
            for (Role role : this.roles) {
                // Ajouter le code du rôle (ex: ROOTADMIN, ADMIN)
                authorities.add(new SimpleGrantedAuthority(role.getCode()));
                
                // Ajouter toutes les permissions du rôle
                if (role.getPermissions() != null) {
                    for (String permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }
            }
        }
        
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getUsername() {
        return identifier;
    }

    @Override
    protected String getIdSeparator() {
        return "";
    }

    @Override
    protected String getPrefix() {
        return "USER";
    }


    public boolean hasPermission(String permission) {
        if (permission == null) return false;
        
        if (this.roles != null) {
            return this.roles.stream()
                .anyMatch(role -> role.getPermissions() != null && 
                    role.getPermissions().contains(permission));
        }
        
        return false;
    }
    
    /**
     * Ajoute un rôle à l'utilisateur
     */
    public void addRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }
    
    /**
     * Retire un rôle de l'utilisateur
     */
    public void removeRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }
    
    /**
     * Retourne tous les codes de permission de l'utilisateur
     */
    public Set<String> getAllPermissions() {
        Set<String> allPermissions = new HashSet<>();
        
        if (this.roles != null) {
            for (Role role : this.roles) {
                if (role.getPermissions() != null) {
                    allPermissions.addAll(role.getPermissions());
                }
            }
        }
        
        return allPermissions;
    }
    
    /**
     * Vérifie si l'utilisateur a au moins un des rôles spécifiés
     */
    public boolean hasAnyRole(String... roleNames) {
        if (roleNames == null || roleNames.length == 0) {
            return false;
        }
        
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        
        return false;
    }

}
