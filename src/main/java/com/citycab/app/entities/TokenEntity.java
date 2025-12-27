package com.citycab.app.entities;

import com.citycab.app.enums.TokenType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenEntity extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String token;

    @Builder.Default
    private TokenType tokenType = TokenType.BEARER;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private boolean expired;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; 
    
    @Override
    protected String getPrefix() {
        return "TOK"; // Préfixe spécifique pour Token
    }

    @Override
    protected boolean includeDateInId(){
        return true;
    }

    protected String getIdSeparator() {
        return "-"; // séparateur par défaut
    }

    protected int getRandomPartLength() {
        return 36; // longueur par défaut de la partie aléatoire
    }
}
