---
lab: 1
topic: "Работа 1: Разработка защищенного REST API с интеграцией в CI/CD"
subject: "Информационная безопасность"
student: "Козьяков Арсений Дмитриевич"
teacher: "Маркина Татьяна Анатольевна"
year: 2026
---

# Работа 1: Разработка защищенного REST API с интеграцией в CI/CD

## Описание проекта

### Используемый стек

- Java 21
- SpringBoot 4.1.1v
- maven

### Архитектура

- [/src](./src/) - код SpringBoot приложения
- [/.github/](./github) - настроенный CI
- [/report-template](./report-template/) - шаблон для генерации отчета
- [/.env.example] - пример для переменных окружения
- [/compose.yaml] - файл для создания docker контейнера с PostgreSQL
- [Makefile] - Makefile для удобства генерации отчета (make pdf)

## Описание API

### Общее описание

Система предназначена для управления студентами, поля: 

- Имя (строка)
- Фамилия (строка)
- Отчество (строка)
- Группа (строка)

Базовый адрес API:

http://localhost:8080

Данные передаются в формате JSON. Для защищённых методов используется HTTP-заголовок:

Authorization: Bearer <JWT>

### Поддерживаемые методы

#### POST /auth/login

Авторизация пользователя и получение JWT-токена

Тело запроса:

```json
{
  "username": "admin",
  "password": "password123"
}
```

Успешный ответ (200 OK):

```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Ошибка (401 Unauthorized): Неверный логин или пароль


#### GET /api/data

Возвращает список всех студентов. Доступ предоставляется только аутентифицированным пользователям.

Пример запроса:

```bash
curl -i http://localhost:8080/api/data \
  -H "Authorization: Bearer <JWT>"
```

Успешный ответ (200 OK):

```json
[
  {
    "id": 1,
    "firstName": "Иван",
    "lastName": "Иванов",
    "patronymic": "Иванович",
    "group": "P3414"
  }
]
```

#### POST /api/data



Тело запроса:
```json
{
  "firstName": "Петр",
  "lastName": "Петров",
  "patronymic": "Петрович", 
  "group": "P3414"
}
```

Пример запроса: 

```bash
curl -i -X POST http://localhost:8080/api/data \
         -H "Authorization: Bearer <JWT>" \
         -H "Content-Type: application/json" \
         -d '{
             "firstName": "Петр",
                 "lastName": "Петров",
                 "patronymic": "Петрович",
                 "group": "P3414"
         }'
```

Успешный ответ (201 Created): Возвращает созданный объект студента с присвоенным id

```json
{
  "id": 2,
  "firstName": "Петр",
  "lastName": "Петров",
  "patronymic": "Петрович",
  "group": "P3414"
}
```
При некорректных данных возвращается 400 Bad Request, а при отсутствии действительного JWT — 401 Unauthorized.

## Описание реализованных мер защиты

### SQLi

Для снижения риска SQL-инъекций в приложении используется Spring Data JPA с Hibernate. Доступ к данным выполняется через репозитории, а SQL-запросы не формируются конкатенацией пользовательских строк.

### XXS

В приложении пользовательские поля студента проходят HTML-экранирование при формировании DTO ответа с помощью HtmlUtils.htmlEscape.

Например, строка:

```html
<script>alert(1)</script>
```

преобразуется в безопасное текстовое представление:

```json
&lt;script&gt;alert(1)&lt;/script&gt;
```

### Реализация аутентификации

Для защиты API от несанкционированного доступа используется Spring Security и механизм JWT.

При успешной аутентификации сервер формирует JWT с использованием алгоритма HS256. Токен подписывается секретным ключом, заданным через переменные окружения.

В токен включаются основные утверждения:

sub — имя пользователя;

iss — издатель токена;

iat — время выдачи;

exp — время истечения срока действия.

При обращении к защищённым эндпоинтам Spring Security проверяет подпись и срок действия токена, а также ожидаемого издателя.


## Тесты

0) Подготовка

```
set -a
source .env
set +a

mvn: clean spring-boot:run
```

1) Успешная аутентификация

```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d "$(jq -n \
    --arg username "$APP_BOOTSTRAP_USERNAME" \
    --arg password "$APP_BOOTSTRAP_PASSWORD" \
    '{username: $username, password: $password}')"
```

Ответ:

```text
HTTP/1.1 200 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 19 Oct 2026 07:17:02 GMT

{"accessToken":"eyJhbGciOiJIUz....","tokenType":"Bearer","expiresIn":3600}
```

**Примечание**: в ответе скоращен accessToken, поскольку он слишком большой и мешает парситься другим полям, вот полное значение:

```text
eyJhbGciOiJIUzI1NiJ9.eyJpc3
MiOiJzdHVkZW50LWFw
aSIsInN1YiI6
ImFkbWluIiwiZ
XhwIjoxNzkyMzk3ODIyLCJpY
XQiOjE3OTIzOTQyMjJ
9.W2UVNphxv0sCFQD9rxR9q4mv8okGH9TyZZH-btH_MJQ
```

2) Отказ при неправильном пароле:

```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d "$(jq -n \
    --arg username "$APP_BOOTSTRAP_USERNAME" \
    --arg password 'wrong-password-123' \
    '{username: $username, password: $password}')"
```

Ответ:

```text
HTTP/1.1 401 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 19 Oct 2026 07:17:51 GMT

{"error":"Invalid username or password"}
```

3) Подготовка токена для последующих запросов:

```bash
TOKEN=$(curl -sS -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d "$(jq -n \
    --arg username "$APP_BOOTSTRAP_USERNAME" \
    --arg password "$APP_BOOTSTRAP_PASSWORD" \
    '{username: $username, password: $password}')" \
  | jq -r '.accessToken // empty')

test -n "$TOKEN" && echo "JWT получен" || echo "Ошибка получения JWT"
```

4) Защита GET от неавторизованного доступа

```bash
curl -i http://localhost:8080/api/data
```

Ответ:

```text
HTTP/1.1 401 
WWW-Authenticate: Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Mon, 19 Oct 2026 07:19:43 GMT
```

5) Получение списка с JWT

```bash
curl -i http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN"
```

Ответ:

```text
HTTP/1.1 200 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 2
Date: Mon, 19 Oct 2026 07:21:13 GMT
```

6) Добавление стунедта с JWT

```bash
curl -i -X POST http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Петр",
    "lastName": "Петров",
    "patronymic": "Петрович",
    "group": "P3414"
  }'
```

Ответ:

```text
HTTP/1.1 201 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 19 Oct 2026 07:22:06 GMT

{"id":1,"firstName":"Петр","lastName":"Петров","patronymic":"Петрович","group":"P3414"}
```

7) Запрет добавления без JWT

```bash
curl -i -X POST http://localhost:8080/api/data \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Unauthorized",
    "lastName": "User",
    "group": "TEST"
  }'
```

Ответ:

```text
HTTP/1.1 401 
WWW-Authenticate: Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Mon, 19 Oct 2026 07:23:05 GMT
```

8) Защита от SQL-инъекции в данных студента

```bash
curl -i -X POST http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "$(jq -n \
    --arg firstName 'Robert' \
    --arg lastName "Robert'); DROP TABLE students;--" \
    --arg group 'P3414' \
    '{
      firstName: $firstName,
      lastName: $lastName,
      patronymic: null,
      group: $group
    }')"
```

```text
HTTP/1.1 201 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 19 Oct 2026 07:25:37 GMT

{"id":2,"firstName":"Robert","lastName":"Robert&#39;); DROP TABLE students;--","patronymic":null,"group":"P3414"}
```

8.1) Проверка доступности таблицы

```bash
curl -i http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN"
```

Ответ:

```text
HTTP/1.1 200 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 227
Date: Mon, 19 Oct 2026 07:26:27 GMT

[{"id":2,"firstName":"Robert","lastName":"Robert&#39;); DROP TABLE students;--","patronymic":null,"group":"P3414"},{"id":1,"firstName":"Петр","lastName":"Петров","patronymic":"Петрович","group":"P3414"}
```

9) Защита от XSS

```bash
curl -i -X POST http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "$(jq -n \
    --arg firstName '<script>alert(1)</script>' \
    --arg lastName 'XSS-Test' \
    --arg group 'XSS-TEST' \
    '{
      firstName: $firstName,
      lastName: $lastName,
      patronymic: null,
      group: $group
    }')"
```

Ответ:

```text
HTTP/1.1 201 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 19 Oct 2026 07:30:07 GMT

{"id":3,"firstName":"&lt;script&gt;alert(1)&lt;/script&gt;","lastName":"XSS-Test","patronymic":null,"group":"XSS-TEST"}
```

10) Проверка валидации данных

```bash
curl -i -X POST http://localhost:8080/api/data \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "lastName": "Test",
    "group": "INVALID-TEST"
  }'
```

Ответ:

```text
HTTP/1.1 400 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 19 Oct 2026 07:31:30 GMT
Connection: close

{"timestamp":"2026-10-19T07:31:30.540Z","status":400,"error":"Bad Request","path":"/api/data"}
```

11) Отклонение подменённого JWT

```bash
TAMPERED_TOKEN="${TOKEN%.*}.invalid-signature"

curl -i http://localhost:8080/api/data \
  -H "Authorization: Bearer $TAMPERED_TOKEN"
```

Ответ:

```text
HTTP/1.1 401 
WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Malformed token", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1", resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Mon, 19 Oct 2026 07:35:27 GMT
```
Все тесты прошли успешно

## Pipeline

[ссылка на последний успешный pipeline](https://github.com/Ch3333zzz/infobez-lab1/actions/runs/39225855639)





