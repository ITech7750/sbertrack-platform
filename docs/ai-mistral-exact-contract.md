# Mistral Exact Contract

Этот appendix фиксирует минимальный контракт для первого релиза ИИ-наставника.

## Request Shape

Для первого релиза используем `chat completions` через HTTP client.

### HTTP

- `POST https://api.mistral.ai/v1/chat/completions`
- `Authorization: Bearer <MISTRAL_API_KEY>`
- `Content-Type: application/json`

### Body

```json
{
  "model": "mistral-small-latest",
  "messages": [
    {
      "role": "system",
      "content": "Ты наставник платформы СберТрек. Помогай думать, задавай вопросы, не решай задачу за пользователя."
    },
    {
      "role": "user",
      "content": "..."
    }
  ],
  "temperature": 0.3,
  "max_tokens": 400
}
```

### Notes

- `model` берём из `MISTRAL_MODEL`.
- `messages` формируются из:
  - system prompt;
  - active master prompt;
  - контекста кейса;
  - истории сообщений;
  - текущего пользовательского сообщения.
- `temperature` держим низкой, чтобы ответ был стабильным и демо-предсказуемым.
- `max_tokens` ограничиваем, чтобы не раздувать стоимость и длину ответа.

## Response Shape

Для первого релиза адаптер возвращает:

- один текст ответа ассистента;
- без tool calling;
- без streaming;
- без отдельного structured output, если не требуется строгий JSON.

Если API вернул ошибку или ответ не подходит по safety/length rules, service layer должен перейти на fallback.

## Fallback Behavior

Fallback работает в таком порядке:

1. Пытаемся вызвать Mistral.
2. Если `401` или `403`, сразу идем в fallback без retry.
3. Если `429`, `502`, `503`, `504` или timeout, делаем один короткий retry только если это допустимо по конфигу.
4. Если retry не помог, возвращаем mock-response из текущего `AgentGatewayPort` fallback.
5. Если и fallback недоступен, возвращаем короткий safe static response.

### Safe static response

Пример:

> Сейчас наставник временно недоступен. Попробуй уточнить цель, ограничения и артефакты задачи, а затем повтори запрос.

## Environment Variables

### Required

- `MISTRAL_API_KEY` - секрет для доступа к API.
- `MISTRAL_MODEL` - модель для первого релиза, по умолчанию `mistral-small-latest`.

### Recommended

- `MISTRAL_BASE_URL` - по умолчанию `https://api.mistral.ai/v1`.
- `MISTRAL_TIMEOUT_MS` - таймаут внешнего запроса.
- `MISTRAL_MAX_TOKENS` - лимит на длину ответа.
- `MISTRAL_TEMPERATURE` - температура генерации.
- `MISTRAL_ENABLE_FALLBACK` - включает fallback на mock.
- `MISTRAL_ENABLE_RETRY` - разрешает один transient retry.

## Acceptance Criteria

Интеграция считается корректной, если:

- студент получает ответ от Mistral;
- при сбое Mistral срабатывает fallback;
- остальная система не меняет поведение;
- ответ короткий, наставнический и не раскрывает готовое решение;
- конфиг можно поменять через env vars без изменения кода.
