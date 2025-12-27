package com.citycab.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citycab.app.entities.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByIdentifier(String identifier);
    Optional<UserEntity> findByEmail(String identifier);
    
    Optional<UserEntity> findByIdentifierAndArchivedAndEnabled(String identifier, Boolean archived, Boolean isEnabled);
}
