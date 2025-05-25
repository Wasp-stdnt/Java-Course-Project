package io.github.wasp_stdnt.passwordmanagerv2.service.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Service
public class PasswordEncryptionService {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordEncryptionService(@Value("${app.encryption.key}") String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(decodedKey, "AES");
    }

    public EncryptedData encrypt(String plaintext) throws GeneralSecurityException {
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        String ciphertext = Base64.getEncoder().encodeToString(encrypted);
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        return new EncryptedData(ciphertext, ivBase64);
    }

    public String decrypt(String ciphertextBase64, String ivBase64) throws GeneralSecurityException {
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        byte[] encrypted = Base64.getDecoder().decode(ciphertextBase64);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public record EncryptedData(String ciphertext, String iv) {}
}
