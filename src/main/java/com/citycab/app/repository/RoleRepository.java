package com.citycab.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citycab.app.entities.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByCode(String code);
    
} 
