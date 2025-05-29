package io.github.wasp_stdnt.passwordmanagerv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    private String token;
    private UserResponseDto user;
}
