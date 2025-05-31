package io.github.wasp_stdnt.passwordmanagerv2.service.impl;

import io.github.wasp_stdnt.passwordmanagerv2.dto.AuthResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.LoginRequestDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.exception.ConflictException;
import io.github.wasp_stdnt.passwordmanagerv2.exception.NotFoundException;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import io.github.wasp_stdnt.passwordmanagerv2.service.UserService;
import io.github.wasp_stdnt.passwordmanagerv2.service.encryption.PasswordHashService;
import io.github.wasp_stdnt.passwordmanagerv2.security.JwtService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordHashService passwordHashService,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationDto registrationDto) {
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new ConflictException("Email already in use");
        }
        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordHashService.hashPassword(registrationDto.getPassword()));
        User saved = userRepository.save(user);
        return UserResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .build();
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordHashService.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new ConflictException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        UserResponseDto userDto = UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        return AuthResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Override
    public UserResponseDto getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
