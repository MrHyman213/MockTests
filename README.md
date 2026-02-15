# Тестирование приложения

## Быстрый старт (Java + Maven)

### 0. Подготовка
⚠️ **Важно:** Убедитесь, что файл `internal-0.0.1-SNAPSHOT.jar` находится в папке проекта

Если файл отсутствует, поместите его туда перед запуском тестов.

### 1. Запустить приложение
```bash
java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
```

### 2. Запустить тесты (в новом терминале)
```bash
mvn test
```

Тесты автоматически:
- Запустят WireMock сервер на порту 8888
- Выполнят все 10 тестовых сценариев
- Остановят WireMock сервер
- Покажут результаты

## Что тестируется

✅ **Позитивные сценарии:**
1. Успешный LOGIN запрос
2. Успешный ACTION запрос
3. Успешный LOGOUT запрос
4. Полный workflow: LOGIN → ACTION → LOGOUT

✅ **Негативные сценарии:**
5. Невалидный формат токена (содержит символы вне 0-9A-F)
6. Отсутствие API ключа (X-Api-Key)
7. Невалидное действие (не LOGIN/ACTION/LOGOUT)
8. Отсутствие параметра token
9. Отсутствие параметра action

✅ **Интеграция с mock-сервером:**
10. Mock-сервер возвращает ошибку 500

## Формат токена

⚠️ **Важно:** Токен должен быть ровно 32 символа в hex-формате: `^[0-9A-F]{32}$`

- ✅ Валидный: `ABCDEF0123456789ABCDEF0123456789`
- ❌ Невалидный: `ABCDEFGHIJKLMNOPQRSTUVWXYZ012345` (содержит G-Z)

## Эндпоинт

```
POST http://localhost:8080/endpoint
Content-Type: application/x-www-form-urlencoded
Accept: application/json
X-Api-Key: qazWSXedc

token=ABCDEF0123456789ABCDEF0123456789&action=LOGIN
```

## Действия

- `LOGIN` - аутентификация → вызывает `/auth` на mock-сервере
- `ACTION` - действие → вызывает `/doAction` на mock-сервере
- `LOGOUT` - завершение сессии

## Ответы

**Успех:**
```json
{"result": "OK"}
```

**Ошибка:**
```json
{"result": "ERROR", "message": "описание"}
```

## Дополнительные команды

**Запустить конкретный тест:**
```bash
mvn test -Dtest=ManualApiTest#testSuccessfulLogin
```

**Детальные логи:**
```bash
mvn test -X
```

**Скомпилировать тесты:**
```bash
mvn test-compile
```

## Требования

- Java 17+
- Maven 3.6+

## Структура тестов

```
src/test/java/org/example/
└── ManualApiTest.java  - Все тесты с WireMock
```

## Зависимости

- JUnit 5 - тестовый фреймворк
- WireMock - mock HTTP сервер
- REST Assured - HTTP клиент для тестов
- AssertJ - fluent assertions
