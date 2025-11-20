package com.reskill.service.user;

import com.reskill.dto.EmailMessage;
import com.reskill.dto.user.CreateUserRequest;
import com.reskill.dto.user.UpdateUserRequest;
import com.reskill.dto.user.UserResponse;
import com.reskill.exception.exceptions.ResourceAlreadyExistsException;
import com.reskill.exception.exceptions.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.reskill.mapper.UserMapper;
import com.reskill.messaging.producer.EmailProducer;
import com.reskill.model.User;
import com.reskill.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailProducer emailProducer;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailProducer emailProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailProducer = emailProducer;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User with this email already exists");
        }

        String encryptedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .name(request.name())
                .email(request.email())
                .password(encryptedPassword)
                .build();

        User saved = userRepository.save(newUser);

        emailProducer.sendEmail(new EmailMessage(
                saved.getEmail(),
                "🎉 Welcome to Mentor.AI!",
                """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #4F46E5;">Welcome to <strong>Mentor.AI</strong> 👋</h2>
        
                    <p>Hello <strong>%s</strong>,</p>
        
                    <p>Your account has been <strong>successfully created</strong> and you are now part of a platform built to help you grow faster with technology, aprendizado e evolução guiada por IA.</p>
        
                    <p>Com o Mentor.AI você poderá:</p>
                    <ul>
                        <li>Receber orientações personalizadas</li>
                        <li>Acompanhar sua evolução em tempo real</li>
                        <li>Melhorar suas habilidades com suporte inteligente</li>
                    </ul>
        
                    <p style="margin-top: 20px;">
                        👉 <a href="https://mentor.ai" style="color: #4F46E5; text-decoration: none; font-weight: bold;">
                            Acessar sua conta
                        </a>
                    </p>
        
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;" />
        
                    <p style="font-size: 12px; color: #666;">
                        Esta é uma mensagem automática do sistema Mentor.AI.  
                    </p>
                </div>
                """.formatted(saved.getName())
        ));

        return UserMapper.toResponse(saved);
    }

    @Override
    public UserResponse getUserById(Long id, User loggedUser) {
        User user = findUserOrThrow(id);
        validateAccess(loggedUser, user);
        return UserMapper.toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserMapper::toResponse);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request, User loggedUser) {
        User user = findUserOrThrow(id);
        validateAccess(loggedUser, user);

        if (!user.getEmail().equals(request.email()) &&
                userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("User with this email already exists");
        }

        user.setName(request.name());
        user.setEmail(request.email());

        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    @Override
    public void deleteUser(Long id, User loggedUser) {
        User user = findUserOrThrow(id);
        validateAccess(loggedUser, user);
        userRepository.delete(user);
    }


    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateAccess(User loggedUser, User targetUser) {
        boolean isAdmin = loggedUser.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !loggedUser.getId().equals(targetUser.getId())) {
            throw new AccessDeniedException("You don't have permission to access another user's data");
        }
    }
}
