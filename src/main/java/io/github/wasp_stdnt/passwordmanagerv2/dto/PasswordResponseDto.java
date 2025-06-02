package io.github.wasp_stdnt.passwordmanagerv2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PasswordResponseDto",
        description = "What the API returns after creating, reading, or updating a password entry"
)
public class PasswordResponseDto {
    @Schema(
            description = "Unique ID of the password entry",
            example = "9"
    )
    private Long id;

    @Schema(
            description = "Name of the service (e.g., Gmail)",
            example = "Gmail"
    )
    private String service;

    @Schema(
            description = "Username/email associated with that service",
            example = "alice@gmail.com"
    )
    private String credential;

    @Schema(
            description = "Decrypted plaintext password",
            example = "MySecretPassword!"
    )
    private String password;
}
