package com.reskill.service.user;

import com.reskill.dto.user.CreateUserRequest;
import com.reskill.dto.user.UpdateUserRequest;
import com.reskill.dto.user.UserResponse;
import com.reskill.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id, User loggedUser);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(Long id, UpdateUserRequest request, User loggedUser);

    void deleteUser(Long id, User loggedUser);
}
