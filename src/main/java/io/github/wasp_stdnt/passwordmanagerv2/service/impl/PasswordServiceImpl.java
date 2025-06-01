package io.github.wasp_stdnt.passwordmanagerv2.service.impl;

import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;
import io.github.wasp_stdnt.passwordmanagerv2.exception.CryptoException;
import io.github.wasp_stdnt.passwordmanagerv2.exception.NotFoundException;
import io.github.wasp_stdnt.passwordmanagerv2.model.Password;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.PasswordRepository;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import io.github.wasp_stdnt.passwordmanagerv2.service.PasswordService;
import io.github.wasp_stdnt.passwordmanagerv2.service.encryption.PasswordEncryptionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PasswordServiceImpl implements PasswordService {
    private final PasswordRepository passwordRepository;
    private final UserRepository userRepository;
    private final PasswordEncryptionService encryptionService;

    public PasswordServiceImpl(PasswordRepository passwordRepository,
                               UserRepository userRepository,
                               PasswordEncryptionService encryptionService) {
        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "passwords", key = "#userId")
    public PasswordResponseDto createPassword(Long userId, PasswordWriteDto createDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        PasswordEncryptionService.EncryptedData data;
        try {
            data = encryptionService.encrypt(createDto.getPassword());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to encrypt password", e);
        }
        Password entity = new Password();
        entity.setService(createDto.getService());
        entity.setCredential(createDto.getCredential());
        entity.setCiphertext(data.ciphertext());
        entity.setIv(data.iv());
        entity.setUser(user);
        Password saved = passwordRepository.save(entity);
        String decrypted;
        try {
            decrypted = encryptionService.decrypt(saved.getCiphertext(), saved.getIv());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to decrypt password after saving", e);
        }
        return PasswordResponseDto.builder()
                .id(saved.getId())
                .service(saved.getService())
                .credential(saved.getCredential())
                .password(decrypted)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "passwords", key = "#userId")
    public List<PasswordResponseDto> listPasswords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return passwordRepository.findByUser(user).stream()
                .map(pw -> {
                    String decrypted;
                    try {
                        decrypted = encryptionService.decrypt(pw.getCiphertext(), pw.getIv());
                    } catch (GeneralSecurityException e) {
                        throw new CryptoException("Failed to decrypt password", e);
                    }
                    return PasswordResponseDto.builder()
                            .id(pw.getId())
                            .service(pw.getService())
                            .credential(pw.getCredential())
                            .password(decrypted)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordResponseDto getPassword(Long userId, Long passwordId) {
        Password pw = passwordRepository.findByIdAndUser(passwordId,
                        userRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("User not found")))
                .orElseThrow(() -> new NotFoundException("Password not found"));
        String decrypted;
        try {
            decrypted = encryptionService.decrypt(pw.getCiphertext(), pw.getIv());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to decrypt password", e);
        }
        return PasswordResponseDto.builder()
                .id(pw.getId())
                .service(pw.getService())
                .credential(pw.getCredential())
                .password(decrypted)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "passwords", key = "#userId")
    public PasswordResponseDto updatePassword(Long userId, Long passwordId, PasswordWriteDto updateDto) {
        Password pw = passwordRepository.findByIdAndUser(passwordId,
                        userRepository.findById(userId)
                                .orElseThrow(() -> new NotFoundException("User not found")))
                .orElseThrow(() -> new NotFoundException("Password not found"));
        PasswordEncryptionService.EncryptedData data;
        try {
            data = encryptionService.encrypt(updateDto.getPassword());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to encrypt password", e);
        }
        pw.setService(updateDto.getService());
        pw.setCredential(updateDto.getCredential());
        pw.setCiphertext(data.ciphertext());
        pw.setIv(data.iv());
        Password updated = passwordRepository.save(pw);
        String decrypted;
        try {
            decrypted = encryptionService.decrypt(updated.getCiphertext(), updated.getIv());
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Failed to decrypt password after update", e);
        }
        return PasswordResponseDto.builder()
                .id(updated.getId())
                .service(updated.getService())
                .credential(updated.getCredential())
                .password(decrypted)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "passwords", key = "#userId")
    public void deletePassword(Long userId, Long passwordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordRepository.existsByIdAndUser(passwordId, user)) {
            throw new NotFoundException("Password not found");
        }
        passwordRepository.deleteByIdAndUser(passwordId, user);
    }
}
