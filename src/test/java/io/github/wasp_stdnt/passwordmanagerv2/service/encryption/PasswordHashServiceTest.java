package io.github.wasp_stdnt.passwordmanagerv2.service.encryption;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordHashServiceTest {

    private PasswordHashService passwordHashService;

    @BeforeEach
    void setUp() {
        passwordHashService = new PasswordHashService();
    }

    @Test
    void hashPassword_shouldProduceNonNullHashDifferentFromRaw() {
        String raw = "TestPassword!";
        String hash = passwordHashService.hashPassword(raw);

        assertThat(hash).isNotBlank();
        assertThat(hash).isNotEqualTo(raw);
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String raw = "AnotherSecret123";
        String hash = passwordHashService.hashPassword(raw);

        assertThat(passwordHashService.matches(raw, hash)).isTrue();
    }

    @Test
    void matches_shouldReturnFalseForIncorrectPassword() {
        String raw = "SecretOne";
        String wrong = "SecretTwo";
        String hash = passwordHashService.hashPassword(raw);

        assertThat(passwordHashService.matches(wrong, hash)).isFalse();
    }
}
