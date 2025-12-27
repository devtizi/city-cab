package com.citycab.app.dtos;

import java.time.LocalDateTime;
import java.util.Set;

import com.citycab.app.entities.Role;
import com.citycab.app.entities.UserEntity;
import com.citycab.app.enums.SocialNetworkAuthProvider;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private String id;

    private String email;
    private String phone;

    private String firstName;
    private String lastName;
    private String identifier;
    private String profileImage;
    private String coverImage;
    private String fcmToken;

    private boolean emailVerified;
    private boolean phoneVerified;

    private String countryCode;
    private String city;
    private String cityId;

    private SocialNetworkAuthProvider provider;

    private Set<Role> roles;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static UserResponse fromEntity(UserEntity user){
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .phone(user.getPhone())
            .build();
    }
}
