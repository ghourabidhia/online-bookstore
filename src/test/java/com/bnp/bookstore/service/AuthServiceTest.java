package com.bnp.bookstore.service;

import com.bnp.bookstore.dto.request.LoginRequest;
import com.bnp.bookstore.dto.request.RegisterRequest;
import com.bnp.bookstore.dto.response.AuthResponse;
import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.mapper.UserMapper;
import com.bnp.bookstore.repositories.UserRepository;
import com.bnp.bookstore.security.JwtService;
import com.bnp.bookstore.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserMapper userMapper;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    AuthenticationManager authenticationManager;
    @InjectMocks
    AuthServiceImpl authService;

    private final RegisterRequest validRegister = new RegisterRequest(
            "John", "Doe", "john@example.com", "password123"
    );

    @Test
    void register_newUser_returnsToken() {
        User mapped = User.builder()
                .email("john@example.com")
                .firstname("John")
                .lastname("Doe")
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(validRegister)).thenReturn(mapped);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(mapped);
        when(jwtService.generateToken(mapped)).thenReturn("jwt-token");

        AuthResponse response = authService.register(validRegister);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(mapped.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(mapped);
    }

    @Test
    void register_duplicateEmail_throwsBusinessException() {
        User existing = User.builder().email("john@example.com").build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.register(validRegister))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        User user = User.builder()
                .id(1L).email("john@example.com").role(Role.USER).build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_userNotFoundAfterAuth_throwsBusinessException() {
        LoginRequest request = new LoginRequest("ghost@example.com", "password123");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_badCredentials_propagatesException() {
        LoginRequest request = new LoginRequest("john@example.com", "wrong");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void register_passwordIsEncodedBeforeSave() {
        User mapped = User.builder().email("john@example.com").build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userMapper.toEntity(validRegister)).thenReturn(mapped);
        when(passwordEncoder.encode("password123")).thenReturn("BCrypt$hash");
        when(userRepository.save(any())).thenReturn(mapped);
        when(jwtService.generateToken(any())).thenReturn("token");

        authService.register(validRegister);

        assertThat(mapped.getPassword()).isEqualTo("BCrypt$hash");
    }
}
