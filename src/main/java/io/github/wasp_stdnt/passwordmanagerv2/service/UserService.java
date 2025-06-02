package io.github.wasp_stdnt.passwordmanagerv2.service;

import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(UserRegistrationDto registrationDto);
    UserResponseDto getById(Long userId);
    void deleteUser(Long userId);
}
