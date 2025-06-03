package io.github.wasp_stdnt.passwordmanagerv2.integration;

import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import io.github.wasp_stdnt.passwordmanagerv2.repository.UserRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserManagementIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private static final String KEYCLOAK_TOKEN_URL =
            "http://localhost:8081/realms/password-manager-realm/protocol/openid-connect/token";

    private static final String CLIENT_ID = "password-manager-api";
    private static final String CLIENT_SECRET = "Fmz3U37vVta7hk0Hhaei1aBHksICAFaW";
    private static final String TEST_USER_EMAIL = "alice@example.com";
    private static final String TEST_USER_PASSWORD = "Password123!";

    private static String adminToken;

    @BeforeAll
    static void waitForKeycloak() throws InterruptedException {
        waitForKeycloakUp();
    }

    private static void waitForKeycloakUp() {
        int maxTries = 10;
        int attempt = 0;
        while (attempt < maxTries) {
            try {
                int status = given()
                        .relaxedHTTPSValidation()
                        .port(8081)
                        .when()
                        .get("/realms/master")
                        .statusCode();
                return;
            } catch (Exception e) {
                attempt++;
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {}
            }
        }
        fail("Keycloak container never came up on port 8081");
    }

    @BeforeEach
    void clearDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    void createUser_success_returnsUserResponseDto() {
        Assertions.assertEquals(0, userRepository.count());

        UserRegistrationDto payload = UserRegistrationDto.builder()
                .name("Bob Test")
                .email("bob.test@example.com")
                .password("Secret123!")
                .build();

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("Bob Test"))
                .body("email", equalTo("bob.test@example.com"));

        Assertions.assertEquals(1, userRepository.count());

        User saved = userRepository.findAll().get(0);
        Assertions.assertEquals("bob.test@example.com", saved.getEmail());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Assertions.assertTrue(
                encoder.matches("Secret123!", saved.getPasswordHash())
        );
    }

    @Test
    @Order(2)
    void createUser_duplicateEmail_returnsConflict() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User existing = new User();
        existing.setName("Existing");
        existing.setEmail("same@email.com");
        existing.setPasswordHash(encoder.encode("Password123!"));
        userRepository.save(existing);

        UserRegistrationDto payload = UserRegistrationDto.builder()
                .name("Another")
                .email("same@email.com")
                .password("AnotherPassword!")
                .build();

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/users")
                .then()
                .statusCode(409)
                .body("code", equalTo("CONFLICT"))
                .body("message", equalTo("Email already in use"));

        Assertions.assertEquals(1, userRepository.count());
    }

    @Test
    @Order(3)
    void createUser_invalidPayload_returnsValidationError() {
        String invalidJson = "{ \"name\": \"NoEmailUser\", \"password\": \"short\" }";

        given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/users")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("code", equalTo("VALIDATION_FAILED"))
                .body("fieldErrors.email", containsString("must not be blank"));
    }

    @Test
    @Order(4)
    void deleteUser_success_returnsNoContent() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User toDelete = new User();
        toDelete.setName("DeleteMe");
        toDelete.setEmail("deleteme@example.com");
        toDelete.setPasswordHash(encoder.encode("Password123!"));
        User saved = userRepository.save(toDelete);
        String token = obtainKeycloakToken(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        given()
                .port(port)
                .auth().oauth2(token)
                .when()
                .delete("/api/users/" + saved.getId())
                .then()
                .statusCode(204);

        Assertions.assertEquals(0, userRepository.count());
    }

    @Test
    @Order(5)
    void deleteUser_notFound_returnsNotFound() {
        String token = obtainKeycloakToken(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        given()
                .port(port)
                .auth().oauth2(token)
                .when()
                .delete("/api/users/9999")
                .then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"))
                .body("message", equalTo("User not found"));
    }

    private String obtainKeycloakToken(String username, String password) {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "password-manager-api")
                .formParam("client_secret", "Fmz3U37vVta7hk0Hhaei1aBHksICAFaW")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post("http://localhost:8081/realms/password-manager-realm/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .extract().path("access_token");
    }
}
