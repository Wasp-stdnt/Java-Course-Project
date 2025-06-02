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
        name = "UserResponseDto",
        description = "What the API returns when you fetch a user’s basic info"
)
public class UserResponseDto {
    @Schema(
            description = "Unique ID of the user",
            example = "3"
    )
    private Long id;

    @Schema(
            description = "Full name of the user",
            example = "Alice Example"
    )
    private String name;

    @Schema(
            description = "Email address of the user",
            example = "alice@example.com"
    )
    private String email;
}
