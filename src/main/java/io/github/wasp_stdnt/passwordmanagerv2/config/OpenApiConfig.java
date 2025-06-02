package io.github.wasp_stdnt.passwordmanagerv2.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Password Manager API",
                version = "v1",
                description = """
            This API allows users to store, retrieve, update, and delete encrypted passwords.
            
            ## Authentication
            All endpoints (except **POST /api/users**) require a Bearer JWT issued by Keycloak.
            Use **Authorize** in the Swagger UI to supply `Bearer <access_token>`.
            """),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter your Keycloak access token as `Bearer <token>`"
)
public class OpenApiConfig { }
