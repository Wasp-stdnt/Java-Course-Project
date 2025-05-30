package io.github.wasp_stdnt.passwordmanagerv2.service.impl;

import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordResponseDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;
import io.github.wasp_stdnt.passwordmanagerv2.exception.NotFoundException;
import io.github.wasp_stdnt.passwordmanagerv2.model.Password;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.PasswordRepository;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import io.github.wasp_stdnt.passwordmanagerv2.service.encryption.PasswordEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock private PasswordRepository passwordRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncryptionService encryptionService;
    @InjectMocks private PasswordServiceImpl passwordService;

    private User user;
    private PasswordWriteDto createDto;
    private PasswordWriteDto updateDto;
    private Password samplePw;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        createDto = PasswordWriteDto.builder()
                .service("Gmail")
                .credential("alice@gmail.com")
                .password("secret")
                .build();

        updateDto = PasswordWriteDto.builder()
                .service("Google")
                .credential("alice.new@gmail.com")
                .password("newsecret")
                .build();

        samplePw = new Password();
        samplePw.setId(2L);
        samplePw.setService("Gmail");
        samplePw.setCredential("alice@gmail.com");
        samplePw.setCiphertext("cipher");
        samplePw.setIv("iv");
        samplePw.setUser(user);
    }

    @Test
    void createPassword_success() throws GeneralSecurityException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        PasswordEncryptionService.EncryptedData data =
                new PasswordEncryptionService.EncryptedData("cipher", "iv");
        when(encryptionService.encrypt("secret")).thenReturn(data);

        Password saved = new Password();
        saved.setId(2L);
        saved.setService("Gmail");
        saved.setCredential("alice@gmail.com");
        saved.setCiphertext("cipher");
        saved.setIv("iv");
        saved.setUser(user);
        when(passwordRepository.save(any())).thenReturn(saved);
        when(encryptionService.decrypt("cipher", "iv")).thenReturn("secret");

        PasswordResponseDto result = passwordService.createPassword(1L, createDto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getService()).isEqualTo("Gmail");
        assertThat(result.getCredential()).isEqualTo("alice@gmail.com");
        assertThat(result.getPassword()).isEqualTo("secret");
        verify(passwordRepository).save(any(Password.class));
    }

    @Test
    void createPassword_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> passwordService.createPassword(1L, createDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void listPasswords_success() throws GeneralSecurityException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordRepository.findByUser(user)).thenReturn(List.of(samplePw));
        when(encryptionService.decrypt("cipher", "iv")).thenReturn("secret");

        List<PasswordResponseDto> list = passwordService.listPasswords(1L);

        assertThat(list).hasSize(1);
        PasswordResponseDto dto = list.get(0);
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getPassword()).isEqualTo("secret");
    }

    @Test
    void getPassword_success() throws GeneralSecurityException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(samplePw));
        when(encryptionService.decrypt("cipher", "iv")).thenReturn("secret");

        PasswordResponseDto dto = passwordService.getPassword(1L, 2L);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getService()).isEqualTo("Gmail");
        assertThat(dto.getPassword()).isEqualTo("secret");
    }

    @Test
    void getPassword_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordRepository.findByIdAndUser(2L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordService.getPassword(1L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Password not found");
    }

    @Test
    void updatePassword_success() throws GeneralSecurityException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(samplePw));
        PasswordEncryptionService.EncryptedData data =
                new PasswordEncryptionService.EncryptedData("newCipher", "newIv");
        when(encryptionService.encrypt("newsecret")).thenReturn(data);

        samplePw.setService("Google");
        samplePw.setCredential("alice.new@gmail.com");
        samplePw.setCiphertext("newCipher");
        samplePw.setIv("newIv");
        when(passwordRepository.save(samplePw)).thenReturn(samplePw);
        when(encryptionService.decrypt("newCipher", "newIv")).thenReturn("newsecret");

        PasswordResponseDto dto = passwordService.updatePassword(1L, 2L, updateDto);

        assertThat(dto.getService()).isEqualTo("Google");
        assertThat(dto.getPassword()).isEqualTo("newsecret");
    }

    @Test
    void deletePassword_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordRepository.existsByIdAndUser(2L, user)).thenReturn(true);

        passwordService.deletePassword(1L, 2L);

        verify(passwordRepository).deleteByIdAndUser(2L, user);
    }

    @Test
    void deletePassword_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordRepository.existsByIdAndUser(2L, user)).thenReturn(false);

        assertThatThrownBy(() -> passwordService.deletePassword(1L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Password not found");
    }
}
