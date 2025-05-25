package io.github.wasp_stdnt.passwordmanagerv2.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResponseDto {
    private Long id;
    private String service;
    private String credential;
    private String password;
}
