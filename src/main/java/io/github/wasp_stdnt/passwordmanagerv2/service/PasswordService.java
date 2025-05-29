package io.github.wasp_stdnt.passwordmanagerv2.service;

import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;

import java.util.List;

public interface PasswordService {
    PasswordResponseDto createPassword(Long userId, PasswordWriteDto createDto);
    List<PasswordResponseDto> listPasswords(Long userId);
    PasswordResponseDto getPassword(Long userId, Long passwordId);
    PasswordResponseDto updatePassword(Long userId, Long passwordId, PasswordWriteDto updateDto);
    void deletePassword(Long userId, Long passwordId);
}
