package io.github.wasp_stdnt.passwordmanagerv2.service.impl;

import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.exception.ConflictException;
import io.github.wasp_stdnt.passwordmanagerv2.exception.NotFoundException;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import io.github.wasp_stdnt.passwordmanagerv2.service.UserService;
import io.github.wasp_stdnt.passwordmanagerv2.service.encryption.PasswordHashService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordHashService passwordHashService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordHashService passwordHashService) {
        this.userRepository = userRepository;
        this.passwordHashService = passwordHashService;
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
