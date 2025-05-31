package io.github.wasp_stdnt.passwordmanagerv2.service.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.wasp_stdnt.passwordmanagerv2.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = "aVeryStrongSecretKeyForJwtSigning123!";
        long expirationMs = 100_000L;
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    void generateAndValidateToken_shouldBeValid() {
        Long userId = 42L;
        String email = "user@example.com";
        String token = jwtService.generateToken(userId, email);

        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void getDecodedToken_shouldReturnCorrectClaims() {
        Long userId = 7L;
        String email = "test@domain.com";
        String token = jwtService.generateToken(userId, email);
        DecodedJWT decoded = jwtService.getDecodedToken(token);

        assertThat(decoded.getSubject()).isEqualTo(userId.toString());
        assertThat(decoded.getClaim("email").asString()).isEqualTo(email);
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalid = "not.a.valid.token";
        assertThat(jwtService.validateToken(invalid)).isFalse();
    }
}
