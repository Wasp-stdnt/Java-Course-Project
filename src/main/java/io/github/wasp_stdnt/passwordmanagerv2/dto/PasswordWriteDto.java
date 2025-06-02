package io.github.wasp_stdnt.passwordmanagerv2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "PasswordWriteDto",
        description = "Sent by clients when creating or updating a password entry"
)
public class PasswordWriteDto {
    @NotBlank
    @Schema(
            description = "Name of the service (e.g., Gmail, GitHub)",
            example = "Gmail"
    )
    private String service;

    @NotBlank
    @Schema(
            description = "Username or email for that service",
            example = "alice@gmail.com"
    )
    private String credential;

    @NotBlank
    @Schema(
            description = "Plaintext password that will be encrypted",
            example = "MySecretPassword!"
    )
    private String password;
}
