package io.github.wasp_stdnt.passwordmanagerv2.service.impl;

import io.github.wasp_stdnt.passwordmanagerv2.dto.AuthResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.LoginRequestDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.exception.ConflictException;
import io.github.wasp_stdnt.passwordmanagerv2.exception.NotFoundException;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import io.github.wasp_stdnt.passwordmanagerv2.service.encryption.PasswordHashService;
import io.github.wasp_stdnt.passwordmanagerv2.service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHashService passwordHashService;
    @Mock private JwtService jwtService;
    @InjectMocks private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;
    private LoginRequestDto loginDto;

    @BeforeEach
    void setUp() {
        registrationDto = UserRegistrationDto.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .build();
        loginDto = new LoginRequestDto("alice@example.com", "password123");
    }

    @Test
    void register_shouldSaveNewUser() {
        when(userRepository.findByEmail(registrationDto.getEmail())).thenReturn(Optional.empty());
        when(passwordHashService.hashPassword("password123")).thenReturn("hashed");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Alice");
        savedUser.setEmail("alice@example.com");
        savedUser.setPasswordHash("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto response = userService.register(registrationDto);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void register_whenEmailExists_shouldThrowConflict() {
        when(userRepository.findByEmail(registrationDto.getEmail()))
                .thenReturn(Optional.of(new User()));
        assertThatThrownBy(() -> userService.register(registrationDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void login_withValidCredentials_shouldReturnAuthResponse() {
        User user = new User();
        user.setId(2L);
        user.setEmail("alice@example.com");
        user.setName("Alice");
        user.setPasswordHash("hashed");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHashService.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(2L, "alice@example.com")).thenReturn("token123");

        AuthResponseDto auth = userService.login(loginDto);

        assertThat(auth.getToken()).isEqualTo("token123");
        assertThat(auth.getUser().getId()).isEqualTo(2L);
        assertThat(auth.getUser().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void login_withUnknownEmail_shouldThrowNotFound() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void login_withInvalidPassword_shouldThrowConflict() {
        User user = new User();
        user.setPasswordHash("hashed");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHashService.matches("password123", "hashed")).thenReturn(false);
        assertThatThrownBy(() -> userService.login(loginDto))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void getById_whenUserExists_shouldReturnUser() {
        User user = new User();
        user.setId(3L);
        user.setName("Bob");
        user.setEmail("bob@example.com");
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        UserResponseDto dto = userService.getById(3L);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void getById_whenNotFound_shouldThrowNotFound() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getById(3L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void deleteUser_whenExists_shouldInvokeDelete() {
        when(userRepository.existsById(4L)).thenReturn(true);
        userService.deleteUser(4L);
        verify(userRepository).deleteById(4L);
    }

    @Test
    void deleteUser_whenNotExists_shouldThrowNotFound() {
        when(userRepository.existsById(4L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(4L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }
}
