package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ManualApiTest {

    private static WireMockServer wireMockServer;
    private static final String WEB_SERVICE_URL = "http://localhost:8080";
    private static final String VALID_API_KEY = "qazWSXedc";
    private static final String VALID_TOKEN = "ABCDEF0123456789ABCDEF0123456789";

    @BeforeAll
    static void startMockServer() {
        wireMockServer = new WireMockServer(options().port(8888));
        wireMockServer.start();
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"success\"}")));
        wireMockServer.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"success\"}")));
        System.out.println("WireMock сервер запущен на http://localhost:8888");
    }

    @AfterAll
    static void stopMockServer() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            System.out.println("WireMock сервер остановлен");
        }
    }

    @BeforeEach
    void resetMockServer() {
        wireMockServer.resetRequests();
    }

    @Test
    @Order(1)
    @DisplayName("1. Успешный LOGIN запрос")
    void testSuccessfulLogin() {
        System.out.println("\n=== Тест 1: Успешный LOGIN запрос ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("result")).isEqualTo("OK");
        wireMockServer.verify(postRequestedFor(urlEqualTo("/auth"))
                .withRequestBody(containing("token=" + VALID_TOKEN)));
    }

    @Test
    @Order(2)
    @DisplayName("2. Успешный ACTION запрос")
    void testSuccessfulAction() {
        System.out.println("\n=== Тест 2: Успешный ACTION запрос ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "ACTION")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("result")).isEqualTo("OK");
        wireMockServer.verify(postRequestedFor(urlEqualTo("/doAction"))
                .withRequestBody(containing("token=" + VALID_TOKEN)));
    }

    @Test
    @Order(3)
    @DisplayName("3. Успешный LOGOUT запрос")
    void testSuccessfulLogout() {
        System.out.println("\n=== Тест 3: Успешный LOGOUT запрос ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("result")).isEqualTo("OK");
    }

    @Test
    @Order(4)
    @DisplayName("4. Полный workflow: LOGIN → ACTION → LOGOUT")
    void testFullWorkflow() {
        System.out.println("\n=== Тест 4: Полный workflow ===");
        System.out.println("Шаг 1: LOGIN");
        Response loginResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("  Status: " + loginResponse.getStatusCode() + ", Response: " + loginResponse.getBody().asString());
        assertThat(loginResponse.getStatusCode()).isEqualTo(200);
        System.out.println("Шаг 2: ACTION");
        Response actionResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "ACTION")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("  Status: " + actionResponse.getStatusCode() + ", Response: " + actionResponse.getBody().asString());
        assertThat(actionResponse.getStatusCode()).isEqualTo(200);
        System.out.println("Шаг 3: LOGOUT");
        Response logoutResponse = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("  Status: " + logoutResponse.getStatusCode() + ", Response: " + logoutResponse.getBody().asString());
        assertThat(logoutResponse.getStatusCode()).isEqualTo(200);
    }

    @Test
    @Order(5)
    @DisplayName("5. Невалидный формат токена (содержит G)")
    void testInvalidTokenFormat() {
        System.out.println("\n=== Тест 5: Невалидный формат токена ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", "ABCDEFG123456789ABCDEF0123456789")
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("result")).isEqualTo("ERROR");
        assertThat(response.jsonPath().getString("message")).contains("должно соответствовать");
    }

    @Test
    @Order(6)
    @DisplayName("6. Отсутствие API ключа")
    void testMissingApiKey() {
        System.out.println("\n=== Тест 6: Отсутствие API ключа ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                // Не передаем X-Api-Key
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.jsonPath().getString("result")).isEqualTo("ERROR");
        assertThat(response.jsonPath().getString("message")).contains("API Key");
    }

    @Test
    @Order(7)
    @DisplayName("7. Невалидное действие")
    void testInvalidAction() {
        System.out.println("\n=== Тест 7: Невалидное действие ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "INVALID")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("result")).isEqualTo("ERROR");
        assertThat(response.jsonPath().getString("message")).contains("invalid action");
    }

    @Test
    @Order(8)
    @DisplayName("8. Отсутствие параметра token")
    void testMissingToken() {
        System.out.println("\n=== Тест 8: Отсутствие параметра token ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("result")).isEqualTo("ERROR");
        assertThat(response.jsonPath().getString("message")).contains("token");
    }

    @Test
    @Order(9)
    @DisplayName("9. Отсутствие параметра action")
    void testMissingAction() {
        System.out.println("\n=== Тест 9: Отсутствие параметра action ===");
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getString("result")).isEqualTo("ERROR");
        assertThat(response.jsonPath().getString("message")).contains("action");
    }

    @Test
    @Order(10)
    @DisplayName("10. Mock-сервер возвращает ошибку 500")
    void testMockServerError() {
        System.out.println("\n=== Тест 10: Mock-сервер возвращает ошибку 500 ===");
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal server error\"}")));
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Response: " + response.getBody().asString());
        assertThat(response.getStatusCode()).isIn(200, 500, 502, 503);
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"success\"}")));
    }
}
