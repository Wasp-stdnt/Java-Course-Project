package io.github.wasp_stdnt.passwordmanagerv2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "UserRegistrationDto",
        description = """
    Sent by clients to register a new user locally.
    ─ `name`: full name
    ─ `email`: must be a valid email
    ─ `password`: 8–64 chars
    """
)
public class UserRegistrationDto {
    @NotBlank
    @Schema(
            description = "Full name of the user",
            example = "Alice Example"
    )
    private String name;

    @Email
    @NotBlank
    @Schema(
            description = "Email address (must match Keycloak username)",
            example = "alice@example.com"
    )
    private String email;

    @NotBlank
    @Size(min = 8, max = 64)
    @Schema(
            description = "Plaintext password (8–64 characters)",
            example = "Password123!"
    )
    private String password;
}
