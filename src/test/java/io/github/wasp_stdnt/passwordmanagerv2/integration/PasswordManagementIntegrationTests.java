package io.github.wasp_stdnt.passwordmanagerv2.integration;

import io.github.wasp_stdnt.passwordmanagerv2.dto.PasswordWriteDto;
import io.github.wasp_stdnt.passwordmanagerv2.dto.UserRegistrationDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PasswordManagementIntegrationTest {

    @LocalServerPort
    int port;

    private static final String KEYCLOAK_URL = "http://localhost:8081";
    private static final String REALM = "password-manager-realm";
    private static final String CLIENT_ID = "password-manager-api";
    private static final String CLIENT_SECRET = "Fmz3U37vVta7hk0Hhaei1aBHksICAFaW";

    private static final String TEST_EMAIL = "alice@example.com";
    private static final String TEST_USER_PASSWORD = "Password123!";

    @BeforeAll
    static void waitForKeycloakUp() {
        int attempts = 0;
        while (attempts < 10) {
            try {
                given()
                        .relaxedHTTPSValidation()
                        .baseUri(KEYCLOAK_URL)
                        .when()
                        .get("/realms/" + REALM + "/protocol/openid-connect/certs")
                        .then()
                        .statusCode(anyOf(is(200), is(404)));
                return;
            } catch (Exception ignore) {
                attempts++;
                try { Thread.sleep(1000); } catch (InterruptedException e) { /* ignore */ }
            }
        }
        throw new IllegalStateException("Keycloak did not start in time on " + KEYCLOAK_URL);
    }

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    void registerLocalUser() {
        UserRegistrationDto reg = UserRegistrationDto.builder()
                .name("Alice")
                .email(TEST_EMAIL)
                .password("ignored-by-Keycloak")
                .build();

        given()
                .contentType("application/json")
                .body(reg)
                .when()
                .post("/api/users")
                .then()
                .statusCode(is(200))
                .body("email", equalTo(TEST_EMAIL))
                .body("name", equalTo("Alice"))
                .body("id", notNullValue());
    }

    String obtainKeycloakToken() {
        Response resp = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type",   "password")
                .formParam("client_id",    CLIENT_ID)
                .formParam("client_secret",CLIENT_SECRET)
                .formParam("username",     TEST_EMAIL)
                .formParam("password",     TEST_USER_PASSWORD)
                .when()
                .post(KEYCLOAK_URL
                        + "/realms/" + REALM
                        + "/protocol/openid-connect/token");

        if (resp.statusCode() != 200) {
            System.err.println("Keycloak token error → HTTP " + resp.statusCode());
            System.err.println("Body: " + resp.getBody().asString());
            Assertions.fail("Could not obtain Keycloak token");
        }

        return resp.jsonPath().getString("access_token");
    }

    String obtainKeycloakToken(String username, String password) {
        Response resp = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type",   "password")
                .formParam("client_id",    CLIENT_ID)
                .formParam("client_secret",CLIENT_SECRET)
                .formParam("username",     username)
                .formParam("password",     password)
                .when()
                .post(KEYCLOAK_URL
                        + "/realms/" + REALM
                        + "/protocol/openid-connect/token");

        if (resp.statusCode() != 200) {
            System.err.println("Keycloak token error → HTTP " + resp.statusCode());
            System.err.println("Body: " + resp.getBody().asString());
            Assertions.fail("Could not obtain Keycloak token for " + username);
        }
        return resp.jsonPath().getString("access_token");
    }

    @Test
    @DisplayName("Create → List → Get → Update → Delete password (happy path)")
    void fullPasswordCrudFlow() {
        registerLocalUser();

        String token = obtainKeycloakToken();

        given()
                .auth().oauth2(token)
                .when()
                .get("/api/passwords")
                .then()
                .statusCode(200)
                .body("", hasSize(0));

        PasswordWriteDto createDto = PasswordWriteDto.builder()
                .service("GitHub")
                .credential("alice@github.com")
                .password("MySecret123!")
                .build();

        Integer newId = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createDto)
                .when()
                .post("/api/passwords")
                .then()
                .statusCode(200)
                .body("service",    equalTo("GitHub"))
                .body("credential", equalTo("alice@github.com"))
                .body("password",   equalTo("MySecret123!"))
                .body("id",         notNullValue())
                .extract()
                .path("id");

        given()
                .auth().oauth2(token)
                .when()
                .get("/api/passwords")
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].service",    equalTo("GitHub"))
                .body("[0].credential", equalTo("alice@github.com"))
                .body("[0].password",   equalTo("MySecret123!"))
                .body("[0].id",         equalTo(newId));

        given()
                .auth().oauth2(token)
                .when()
                .get("/api/passwords/{id}", newId)
                .then()
                .statusCode(200)
                .body("service",    equalTo("GitHub"))
                .body("credential", equalTo("alice@github.com"))
                .body("password",   equalTo("MySecret123!"))
                .body("id",         equalTo(newId));

        PasswordWriteDto updateDto = PasswordWriteDto.builder()
                .service("GitHub")
                .credential("alice@github.com")
                .password("MyUpdatedSecret!")
                .build();

        given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(updateDto)
                .when()
                .put("/api/passwords/{id}", newId)
                .then()
                .statusCode(200)
                .body("password", equalTo("MyUpdatedSecret!"))
                .body("service",  equalTo("GitHub"))
                .body("credential", equalTo("alice@github.com"))
                .body("id", equalTo(newId));

        given()
                .auth().oauth2(token)
                .when()
                .delete("/api/passwords/{id}", newId)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(token)
                .when()
                .get("/api/passwords/{id}", newId)
                .then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"))
                .body("message", equalTo("Password not found"));
    }

    @Test
    @DisplayName("Create password → delete with wrong user → 404")
    void deletePasswordAsWrongUser_returnsNotFound() {
        registerLocalUser();
        String token = obtainKeycloakToken();

        PasswordWriteDto createDto = PasswordWriteDto.builder()
                .service("Slack")
                .credential("alice@slack.com")
                .password("SlackPW!")
                .build();

        Integer pwId = given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createDto)
                .when()
                .post("/api/passwords")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        UserRegistrationDto bobReg = UserRegistrationDto.builder()
                .name("Bob")
                .email("bob+" + UUID.randomUUID() + "@example.com")
                .password("whatever")
                .build();

        given()
                .contentType("application/json")
                .body(bobReg)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200);

        String bobToken = obtainKeycloakToken("bob@example.com", "PasswordBob!");

        given()
                .auth().oauth2(bobToken)
                .when()
                .delete("/api/passwords/{id}", pwId)
                .then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"))
                .body("message", equalTo("Password not found"));
    }
}
