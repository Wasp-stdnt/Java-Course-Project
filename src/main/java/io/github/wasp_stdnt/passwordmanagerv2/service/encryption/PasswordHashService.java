package io.github.wasp_stdnt.passwordmanagerv2.service.encryption;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {
    private final BCryptPasswordEncoder encoder;

    public PasswordHashService() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
