package com.reskill.controller;

import com.reskill.dto.user.CreateUserRequest;
import com.reskill.dto.user.UpdateUserRequest;
import com.reskill.dto.user.UserResponse;
import com.reskill.model.User;
import com.reskill.service.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Operation(
            summary = "Create a new user",
            description = "Registers a new user in the system.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid data")
            }
    )
    @PostMapping("/register")
    @CacheEvict(value = "users", allEntries = true)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "List all users",
            description = "Returns a paginated list of users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List returned successfully")
            }
    )
    @GetMapping
    @Cacheable("users")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        Page<UserResponse> result = userService.getAllUsers(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns user information based on the provided ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal User loggedUser
    ) {
        UserResponse dto = userService.getUserById(id, loggedUser);
        return ResponseEntity.ok(dto);
    }


    @Operation(
            summary = "Update user",
            description = "Updates an existing user with new information.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid data"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PutMapping("/{id}")
    @CacheEvict(value = "users", allEntries = true)
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User loggedUser
    ) {
        UserResponse updated = userService.updateUser(id, request, loggedUser);
        return ResponseEntity.ok(updated);
    }


    @Operation(
            summary = "Delete user",
            description = "Deletes a user based on the provided ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{id}")
    @CacheEvict(value = "users", allEntries = true)
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User loggedUser
    ) {
        userService.deleteUser(id, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
