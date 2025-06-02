package io.github.wasp_stdnt.passwordmanagerv2.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Schema(
        name = "ValidationErrorResponse",
        description = "Returned when a request fails bean-validation. Contains a code and a map of field → message."
)
public class ValidationErrorResponse {
    @Schema(
            description = "Fixed error code indicating validation failure",
            example = "VALIDATION_FAILED"
    )
    private String code;

    @Schema(
            description = "Map from invalid field names to their validation error messages",
            example = "{\"email\":\"must be a well-formed email address\",\"password\":\"size must be between 8 and 64\"}"
    )
    private Map<String, String> fieldErrors;
}
