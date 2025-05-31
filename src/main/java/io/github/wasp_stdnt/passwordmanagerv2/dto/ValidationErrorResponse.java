package io.github.wasp_stdnt.passwordmanagerv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private String code;
    private Map<String, String> fieldErrors;
}
