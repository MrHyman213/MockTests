package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Testing")
@Feature("Endpoint Testing")
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
    @Description("Проверка успешной аутентификации пользователя через LOGIN действие")
    @Story("Позитивные сценарии")
    @Severity(SeverityLevel.BLOCKER)
    void testSuccessfulLogin() {
        String token = VALID_TOKEN;
        String action = "LOGIN";
        
        Allure.parameter("Token", token);
        Allure.parameter("Action", action);
        Allure.parameter("API Key", VALID_API_KEY);
        
        Response response = sendRequest(token, action);
        
        verifySuccessResponse(response);
    }

    @Test
    @Order(2)
    @DisplayName("2. Успешный ACTION запрос")
    @Description("Проверка успешного выполнения ACTION для аутентифицированного пользователя")
    @Story("Позитивные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    void testSuccessfulAction() {
        String token = VALID_TOKEN;
        String action = "ACTION";
        
        Allure.parameter("Token", token);
        Allure.parameter("Action", action);
        Allure.parameter("API Key", VALID_API_KEY);
        
        Response response = sendRequest(token, action);
        
        verifySuccessResponse(response);
    }

    @Test
    @Order(3)
    @DisplayName("3. Успешный LOGOUT запрос")
    @Description("Проверка успешного завершения сессии пользователя через LOGOUT")
    @Story("Позитивные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    void testSuccessfulLogout() {
        String token = VALID_TOKEN;
        String action = "LOGOUT";
        
        Allure.parameter("Token", token);
        Allure.parameter("Action", action);
        Allure.parameter("API Key", VALID_API_KEY);
        
        Response response = sendRequest(token, action);
        
        verifySuccessResponse(response);
    }

    @Test
    @Order(4)
    @DisplayName("4. Полный workflow: LOGIN → ACTION → LOGOUT")
    @Description("Проверка полного жизненного цикла пользовательской сессии")
    @Story("Позитивные сценарии")
    @Severity(SeverityLevel.BLOCKER)
    void testFullWorkflow() {
        String token = VALID_TOKEN;
        
        Allure.parameter("Token", token);
        Allure.parameter("API Key", VALID_API_KEY);
        
        Response loginResponse = performLogin(token);
        verifySuccessResponse(loginResponse);
        
        Response actionResponse = performAction(token);
        verifySuccessResponse(actionResponse);
        
        Response logoutResponse = performLogout(token);
        verifySuccessResponse(logoutResponse);
    }

    @Test
    @Order(5)
    @DisplayName("5. Невалидный формат токена (содержит G)")
    @Description("Проверка обработки токена с недопустимыми символами (вне диапазона 0-9A-F)")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    void testInvalidTokenFormat() {
        String invalidToken = "ABCDEFG123456789ABCDEF0123456789";
        String action = "LOGIN";
        
        Allure.parameter("Token (невалидный)", invalidToken);
        Allure.parameter("Action", action);
        Allure.parameter("Причина невалидности", "Содержит символ 'G' (допустимы только 0-9A-F)");
        
        setupMockForInvalidToken(invalidToken);
        
        Response response = sendRequest(invalidToken, action);
        
        verifyErrorResponse(response, 400, "должно соответствовать");
    }

    @Test
    @Order(6)
    @DisplayName("6. Отсутствие API ключа")
    @Description("Проверка обработки запроса без обязательного заголовка X-Api-Key")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    void testMissingApiKey() {
        String token = VALID_TOKEN;
        String action = "LOGIN";
        
        Allure.parameter("Token", token);
        Allure.parameter("Action", action);
        Allure.parameter("API Key", "НЕ ПЕРЕДАН");
        
        setupMockForMissingApiKey();
        
        Response response = sendRequestWithoutApiKey(token, action);
        
        verifyErrorResponse(response, 401, "API Key");
    }

    @Test
    @Order(7)
    @DisplayName("7. Невалидное действие")
    @Description("Проверка обработки неподдерживаемого значения параметра action")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    void testInvalidAction() {
        String token = VALID_TOKEN;
        String invalidAction = "INVALID";
        
        Allure.parameter("Token", token);
        Allure.parameter("Action (невалидный)", invalidAction);
        Allure.parameter("Допустимые значения", "LOGIN, ACTION, LOGOUT");
        
        setupMockForInvalidAction();
        
        Response response = sendRequest(token, invalidAction);
        
        verifyErrorResponse(response, 400, "invalid action");
    }

    @Test
    @Order(8)
    @DisplayName("8. Отсутствие параметра token")
    @Description("Проверка обработки запроса без обязательного параметра token")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    void testMissingToken() {
        String action = "LOGIN";
        
        Allure.parameter("Token", "НЕ ПЕРЕДАН");
        Allure.parameter("Action", action);
        
        setupMockForMissingToken();
        
        Response response = sendRequestWithoutToken(action);
        
        verifyErrorResponse(response, 400, "token");
    }

    @Test
    @Order(9)
    @DisplayName("9. Отсутствие параметра action")
    @Description("Проверка обработки запроса без обязательного параметра action")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.CRITICAL)
    void testMissingAction() {
        String token = VALID_TOKEN;
        
        Allure.parameter("Token", token);
        Allure.parameter("Action", "НЕ ПЕРЕДАН");
        
        setupMockForMissingAction();
        
        Response response = sendRequestWithoutAction(token);
        
        verifyErrorResponse(response, 400, "action");
    }

    @Test
    @Order(10)
    @DisplayName("10. Mock-сервер возвращает ошибку 500")
    @Description("Проверка обработки внутренней ошибки сервера")
    @Story("Негативные сценарии")
    @Severity(SeverityLevel.NORMAL)
    void testMockServerError() {
        String token = VALID_TOKEN;
        String action = "LOGIN";
        
        Allure.parameter("Token", token);
        Allure.parameter("Action", action);
        Allure.parameter("Ожидаемый статус", "500 Internal Server Error");
        
        setupMockForServerError();
        
        Response response = sendRequest(token, action);
        
        verifyStatusCode(response, 500);
    }
    
    // ========== Вспомогательные методы с аннотацией @Step ==========
    
    @Step("Отправка POST запроса: token={token}, action={action}")
    private Response sendRequest(String token, String action) {
        return given()
                .filter(new AllureRestAssured())
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
    }
    
    @Step("Отправка POST запроса БЕЗ API ключа: token={token}, action={action}")
    private Response sendRequestWithoutApiKey(String token, String action) {
        return given()
                .filter(new AllureRestAssured())
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post(WEB_SERVICE_URL + "/endpoint-no-key");
    }
    
    @Step("Отправка POST запроса БЕЗ параметра token: action={action}")
    private Response sendRequestWithoutToken(String action) {
        return given()
                .filter(new AllureRestAssured())
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("action", action)
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
    }
    
    @Step("Отправка POST запроса БЕЗ параметра action: token={token}")
    private Response sendRequestWithoutAction(String token) {
        return given()
                .filter(new AllureRestAssured())
                .contentType("application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-Api-Key", VALID_API_KEY)
                .formParam("token", token)
                .when()
                .post(WEB_SERVICE_URL + "/endpoint");
    }
    
    @Step("Шаг 1: Выполнение LOGIN для token={token}")
    private Response performLogin(String token) {
        return sendRequest(token, "LOGIN");
    }
    
    @Step("Шаг 2: Выполнение ACTION для token={token}")
    private Response performAction(String token) {
        return sendRequest(token, "ACTION");
    }
    
    @Step("Шаг 3: Выполнение LOGOUT для token={token}")
    private Response performLogout(String token) {
        return sendRequest(token, "LOGOUT");
    }
    
    @Step("Проверка успешного ответа: статус 200, result=OK")
    private void verifySuccessResponse(Response response) {
        Allure.step("Проверка статус-кода 200", () -> {
            assertThat(response.getStatusCode()).isEqualTo(200);
        });
        
        Allure.step("Проверка поля result=OK", () -> {
            assertThat(response.jsonPath().getString("result")).isEqualTo("OK");
        });
        
        Allure.attachment("Response Body", response.getBody().asString());
    }
    
    @Step("Проверка ответа с ошибкой: статус {expectedStatus}, сообщение содержит '{messageFragment}'")
    private void verifyErrorResponse(Response response, int expectedStatus, String messageFragment) {
        Allure.step("Проверка статус-кода " + expectedStatus, () -> {
            assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        });
        
        Allure.step("Проверка поля result=ERROR", () -> {
            assertThat(response.jsonPath().getString("result")).isEqualTo("ERROR");
        });
        
        Allure.step("Проверка сообщения об ошибке содержит: " + messageFragment, () -> {
            assertThat(response.jsonPath().getString("message")).contains(messageFragment);
        });
        
        Allure.attachment("Response Body", response.getBody().asString());
    }
    
    @Step("Проверка статус-кода {expectedStatus}")
    private void verifyStatusCode(Response response, int expectedStatus) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        Allure.attachment("Response Body", response.getBody().asString());
    }
    
    @Step("Настройка mock для невалидного токена")
    private void setupMockForInvalidToken(String invalidToken) {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(containing(invalidToken.substring(0, 7)))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Токен должно соответствовать формату\"}")));
    }
    
    @Step("Настройка mock для отсутствующего API ключа")
    private void setupMockForMissingApiKey() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint-no-key"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Missing API Key\"}")));
    }
    
    @Step("Настройка mock для невалидного действия")
    private void setupMockForInvalidAction() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(containing("action=INVALID"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"invalid action\"}")));
    }
    
    @Step("Настройка mock для отсутствующего параметра token")
    private void setupMockForMissingToken() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(notMatching(".*token=.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Missing token parameter\"}")));
    }
    
    @Step("Настройка mock для отсутствующего параметра action")
    private void setupMockForMissingAction() {
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .withRequestBody(notMatching(".*action=.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\": \"ERROR\", \"message\": \"Missing action parameter\"}")));
    }
    
    @Step("Настройка mock для ошибки сервера 500")
    private void setupMockForServerError() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlEqualTo("/endpoint"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal server error\"}")));
    }
}
