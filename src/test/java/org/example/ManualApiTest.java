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
    private static final String WEB_SERVICE_URL = "http://localhost:8888";
    private static final String VALID_API_KEY = "qazWSXedc";
    private static final String VALID_TOKEN = "ABCDEF0123456789ABCDEF0123456789";

    @BeforeAll
    static void startMockServer() {
        wireMockServer = new WireMockServer(options().port(8888));
        wireMockServer.start();
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"OK\"}")));
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
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(containing("ABCDEFG"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Токен должно соответствовать формату\"}")));
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
        wireMockServer.stubFor(post(urlEqualTo("/endpoint-no-key"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Missing API Key\"}")));
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                // Не передаем X-Api-Key
                .formParam("token", VALID_TOKEN)
                .formParam("action", "LOGIN")
                .when()
                .post(WEB_SERVICE_URL + "/endpoint-no-key");
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
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(containing("action=INVALID"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"invalid action\"}")));
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
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(notMatching(".*token=.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Missing token parameter\"}")));
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
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(notMatching(".*action=.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Missing action parameter\"}")));
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
        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
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
        assertThat(response.getStatusCode()).isEqualTo(500);
    }
}
