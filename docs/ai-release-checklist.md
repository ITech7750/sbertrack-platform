# AI Integration Release Checklist

Короткий чеклист для выпуска интеграции ИИ-наставника без переусложнения системы.

## Scope

- Один provider: Mistral.
- Один сценарий: ответы наставника в `AgentService`.
- Один fallback: текущий mock-ответ.
- Без изменения рабочих частей платформы вне `agent`-контура.
- Подключение через HTTP client, не через SDK.

## Integration Method

- Используем `WebClient` или `RestClient` из Spring.
- Не тянем third-party Kotlin SDK.
- Не добавляем отдельный integration framework.
- Ограничиваем интеграцию одним адаптером вокруг Mistral API.

## Why HTTP Client

- Минимальный scope.
- Меньше зависимостей.
- Проще удержать изменения в одном модуле.
- Проще тестировать и безопаснее заменять провайдера.

## Before Release

1. `MISTRAL_API_KEY` задан как secret.
2. `MISTRAL_MODEL` настроен на выбранную demo-модель.
3. Timeout на запросы включен.
4. Обработаны `401`, `403`, `429`, `5xx`.
5. Fallback на mock работает.
6. Ответ наставника короткий, структурный и не решает задачу за пользователя.
7. Логи показывают provider, latency и ошибку при fallback.
8. Demo-сценарий проходит от `sign-in` до сообщения наставнику.

## Demo Acceptance

- Пользователь входит в систему.
- Открывает кейс.
- Пишет сообщение наставнику.
- Получает ответ от Mistral.
- При проблеме с API получает безопасный fallback.
- Остальной функционал платформы продолжает работать.

## Do Not Ship If

- ИИ отвечает вместо моков, но без fallback.
- Ответы слишком длинные или нестабильные.
- Для демо требуется ручная правка конфигов в процессе показа.
- Интеграция задевает unrelated modules.

## Contract Appendix

Точный request shape, fallback flow и перечень env vars:

- [Mistral Exact Contract](./ai-mistral-exact-contract.md)
