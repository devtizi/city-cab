package com.citycab.app.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.citycab.app.entities.TokenEntity;

public interface TokenRepository extends JpaRepository<TokenEntity, String> {
    
    Optional<TokenEntity> findByToken(String token);

    // CORRECTION : Utilisez t.userId.id au lieu de t.user.id
    @Query("SELECT t FROM TokenEntity t WHERE t.user.id = :userId AND t.expired = false AND t.revoked = false")
    List<TokenEntity> findAllValidTokenByUser(@Param("userId") String userId);
    
    // Cette méthode devrait fonctionner car elle utilise la convention de nommage correcte
    List<TokenEntity> findByUserId_IdAndExpiredFalseAndRevokedFalse(String userId);
    
    List<TokenEntity> findAllByUserId_Id(String userId);
}