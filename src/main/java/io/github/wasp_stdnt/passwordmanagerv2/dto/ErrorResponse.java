package io.github.wasp_stdnt.passwordmanagerv2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(
        name = "ErrorResponse",
        description = "Generic error response with a code and human-readable message"
)
public class ErrorResponse {
    @Schema(
            description = "Error code identifying the failure category",
            example = "NOT_FOUND"
    )
    private String code;

    @Schema(
            description = "Detailed message explaining the error",
            example = "User not found"
    )
    private String message;
}
