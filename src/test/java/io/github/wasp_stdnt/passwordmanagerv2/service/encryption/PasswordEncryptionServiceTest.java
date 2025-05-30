package io.github.wasp_stdnt.passwordmanagerv2.service.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncryptionServiceTest {

    private PasswordEncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        encryptionService = new PasswordEncryptionService(base64Key);
    }

    @Test
    void encryptDecrypt_shouldReturnOriginal() throws GeneralSecurityException {
        String plaintext = "mySecretPassword!";
        var encryptedData = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encryptedData.ciphertext(), encryptedData.iv());
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encrypt_shouldProduceDifferentCiphertextWithDifferentIv() throws GeneralSecurityException {
        String plaintext = "mySecretPassword!";
        var data1 = encryptionService.encrypt(plaintext);
        var data2 = encryptionService.encrypt(plaintext);
        assertThat(data1.ciphertext()).isNotEqualTo(data2.ciphertext());
        assertThat(data1.iv()).isNotEqualTo(data2.iv());
    }
}
