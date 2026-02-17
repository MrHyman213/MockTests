# API Testing Project with Allure Reports

Проект для тестирования REST API с использованием WireMock и генерацией красивых отчетов через Allure.

## Требования

- Java 17+
- Maven 3.6+

## Быстрый старт

### Запустить тесты и открыть Allure отчет
```bash
mvn clean test allure:serve
```

После выполнения команды автоматически откроется браузер с интерактивным отчетом.

### Только сгенерировать отчет (без открытия)
```bash
mvn allure:report
```

Отчет будет доступен в `target/site/allure-maven-plugin/index.html`

## Что тестируется

### ✅ Позитивные сценарии
1. Успешный LOGIN запрос
2. Успешный ACTION запрос
3. Успешный LOGOUT запрос
4. Полный workflow: LOGIN → ACTION → LOGOUT

### ❌ Негативные сценарии
5. Невалидный формат токена (содержит символы вне 0-9A-F)
6. Отсутствие API ключа (X-Api-Key)
7. Невалидное действие (не LOGIN/ACTION/LOGOUT)
8. Отсутствие параметра token
9. Отсутствие параметра action
10. Mock-сервер возвращает ошибку 500

## API Эндпоинт

```http
POST http://localhost:8888/endpoint
Content-Type: application/x-www-form-urlencoded
Accept: application/json
X-Api-Key: qazWSXedc

token=ABCDEF0123456789ABCDEF0123456789&action=LOGIN
```

## Действия (Actions)

- `LOGIN` - аутентификация
- `ACTION` - выполнение действия
- `LOGOUT` - завершение сессии

## Формат ответов

**Успех:**
```json
{"result": "OK"}
```

**Ошибка:**
```json
{"result": "ERROR", "message": "описание ошибки"}
```

## Технологии

- **JUnit 5** - тестовый фреймворк
- **WireMock** - mock HTTP сервер
- **REST Assured** - HTTP клиент для тестов
- **AssertJ** - fluent assertions
- **Allure** - генерация красивых отчетов

Все тесты изолированы и могут выполняться в любом порядке.
