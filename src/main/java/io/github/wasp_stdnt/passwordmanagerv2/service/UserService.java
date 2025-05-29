package io.github.wasp_stdnt.passwordmanagerv2.service;

import io.github.wasp_stdnt.passwordmanagerv2.dto.AuthResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.LoginRequestDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserResponseDto;

public interface UserService {
    UserResponseDto register(UserRegistrationDto registrationDto);
    AuthResponseDto login(LoginRequestDto loginRequestDto);
    UserResponseDto getById(Long userId);
    void deleteUser(Long userId);
}
