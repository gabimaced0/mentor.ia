package com.reskill.mapper;

import com.reskill.dto.user.CreateUserRequest;
import com.reskill.dto.user.UpdateUserRequest;
import com.reskill.dto.user.UserResponse;
import com.reskill.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public static User toEntity(CreateUserRequest request) {
        return User.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .createdAt(LocalDateTime.now())
                .build();
    }


    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
