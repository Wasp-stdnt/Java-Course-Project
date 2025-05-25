package io.github.wasp_stdnt.passwordmanagerv2.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordWriteDto {
    @NotBlank
    private String service;

    @NotBlank
    private String credential;

    @NotBlank
    private String password;
}
