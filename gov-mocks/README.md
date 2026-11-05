# gov-mocks — заглушки гос-сервисов (WireMock)

## Запуск
```bash
docker compose up -d
# мок слушает http://localhost:8089
# все маппинги:     http://localhost:8089/__admin/mappings
# журнал запросов:  http://localhost:8089/__admin/requests
```
После правки файлов: `docker compose restart`.

Структура:
```
gov-mocks/
├── docker-compose.yml
├── mappings/mkb/      # правила «какой запрос -> какой ответ»
└── __files/mkb/       # большие тела ответов (шаблоны)
```

## МКБ: person-use-token (согласие на доступ к ПД через SMS 1414)

Реальное SMS мок не отправляет. Ответ клиента (511 / 512 / молчание) выбирается **порядковым номером в ИИН** — цифры 8–11:

| Запрос | Ответ |
|---|---|
| нет `x-api-key` | 401, `Учетные данные не были предоставлены.` |
| ИИН пустой, не 12 цифр, буквы, нет поля | 400, `params.iin обязателен (12 цифр)` |
| ИИН с номером **91xx** — клиент ответил 512 | 502, `kdp_status: INVALID` |
| ИИН с номером **90xx** — клиент ещё не ответил | 202, `kdp_status: PENDING` |
| любой другой ИИН из 12 цифр — клиент ответил 511 | 200 + данные из ГБДФЛ |

В ответе 200 **дата рождения и пол вычисляются из ИИН**, ФИО — ТЕСТОВ ТЕСТ ТЕСТОВИЧ / ТЕСТОВА ТЕСТА ТЕСТОВНА,
удостоверение действительно ещё 8 лет от сегодняшнего дня, id сообщений и номер документа случайные.

В автотестах ИИН генерируется так:
```java
IinGenerator.generate(birthDate, Gender.MALE);                          // согласие
IinGenerator.generate(birthDate, Gender.MALE, KdpScenario.PENDING);     // ждём SMS
IinGenerator.generate(birthDate, Gender.FEMALE, KdpScenario.DECLINED);  // отказ
```

Проверка вручную:
```bash
# 200, мужчина 15.03.1985
curl -s -X POST localhost:8089/api/v1/soap/mkb/person-use-token \
  -H 'x-api-key: test' -H 'Content-Type: application/json' \
  -d '{"params":{"iin":"850315300123"},"is_test":false,"request_kdp":true}'

# 502 отказ (номер 9145)
curl -i -X POST localhost:8089/api/v1/soap/mkb/person-use-token \
  -H 'x-api-key: test' -H 'Content-Type: application/json' \
  -d '{"params":{"iin":"850315391459"},"is_test":false,"request_kdp":true}'

# 202 ожидание (номер 90xx)
curl -i -X POST localhost:8089/api/v1/soap/mkb/person-use-token \
  -H 'x-api-key: test' -H 'Content-Type: application/json' \
  -d '{"params":{"iin":"850315390011"},"is_test":false,"request_kdp":true}'

# 401
curl -i -X POST localhost:8089/api/v1/soap/mkb/person-use-token \
  -H 'Content-Type: application/json' -d '{"params":{"iin":"850315300123"}}'
```

## Ограничения
- В реальности первый запрос всегда даёт 202 PENDING, и только повторный после ответа клиента — 200 или 502.
  Мок отвечает сразу итоговым результатом.
- Неверный (не пустой) `x-api-key` мок принимает — поведение реального сервиса не известно.
