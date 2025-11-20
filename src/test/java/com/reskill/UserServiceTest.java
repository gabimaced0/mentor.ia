package com.reskill;

import com.reskill.dto.EmailMessage;
import com.reskill.dto.user.CreateUserRequest;
import com.reskill.dto.user.UpdateUserRequest;
import com.reskill.exception.exceptions.ResourceAlreadyExistsException;
import com.reskill.exception.exceptions.ResourceNotFoundException;
import com.reskill.messaging.producer.EmailProducer;
import com.reskill.model.User;
import com.reskill.repository.UserRepository;
import com.reskill.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailProducer emailProducer;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_success() {
        CreateUserRequest request = new CreateUserRequest("Rafael", "rafael@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encrypted");
        User savedUser = User.builder().id(1L).name(request.name()).email(request.email()).password("encrypted").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var response = userService.createUser(request);

        assertEquals(savedUser.getName(), response.name());
        assertEquals(savedUser.getEmail(), response.email());
        verify(emailProducer, times(1)).sendEmail(any(EmailMessage.class));
    }

    @Test
    void createUser_emailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("Rafael", "rafael@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
        verify(emailProducer, never()).sendEmail(any());
    }

    @Test
    void getUserById_success() {
        User loggedUser = User.builder().id(1L).build();
        User targetUser = User.builder().id(1L).name("Rafael").email("rafael@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        var response = userService.getUserById(1L, loggedUser);

        assertEquals(targetUser.getName(), response.name());
        assertEquals(targetUser.getEmail(), response.email());
    }

    @Test
    void getUserById_notFound() {
        User loggedUser = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L, loggedUser));
    }

    @Test
    void updateUser_success() {
        User loggedUser = User.builder().id(1L).build();
        User user = User.builder().id(1L).name("Old").email("old@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest("New Namee", "new@example.com");
        var response = userService.updateUser(1L, request, loggedUser);

        assertEquals("New Namee", response.name());
        assertEquals("new@example.com", response.email());
    }

    @Test
    void updateUser_emailAlreadyExists() {
        User loggedUser = User.builder().id(1L).build();
        User user = User.builder().id(1L).name("Old").email("old@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        UpdateUserRequest request = new UpdateUserRequest("New Name", "new@example.com");

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.updateUser(1L, request, loggedUser));
    }

    @Test
    void deleteUser_success() {
        User loggedUser = User.builder().id(1L).build();
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, loggedUser);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_accessDenied() {
        User loggedUser = User.builder().id(2L).build();
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(1L, loggedUser));
    }
}
