# Mistral And Project Contracts

Этот документ фиксирует две вещи:

1. какие официальные материалы Mistral нужно держать под рукой;
2. какие внутренние контракты нашей платформы участвуют в интеграции ИИ-наставника.

## Mistral Docs

Ориентиры для реализации через HTTP client:

- [Send your first API request](https://docs.mistral.ai/getting-started/quickstarts/developer/first-api-request)
- [Activate Studio and generate an API key](https://docs.mistral.ai/getting-started/quickstarts/studio/activate-and-generate-api-key)
- [Chat endpoints](https://docs.mistral.ai/api/endpoint/chat)
- [Usage and limits](https://docs.mistral.ai/admin/billing-usage/usage-limits)
- [Rate limits and usage tiers](https://docs.mistral.ai/admin/user-management-finops/tier)
- [Known limitations](https://docs.mistral.ai/resources/known-limitations)
- [Moderation & Guardrailing](https://docs.mistral.ai/studio-api/conversations/moderation)
- [Structured Outputs](https://docs.mistral.ai/studio-api/conversations/structured-output/custom)
- [Function Calling](https://docs.mistral.ai/studio-api/conversations/function-calling)
- [SDKs](https://docs.mistral.ai/resources/sdks)

## Why these docs matter

- `first-api-request` and `chat endpoints` define the basic HTTP shape we need.
- `usage and limits` and `rate limits` explain what can break in free mode.
- `moderation` and `structured outputs` are the optional safety tools if we need stricter responses later.
- `sdks` confirms that official SDKs are Python and TypeScript, so for Kotlin the practical path is HTTP client.

## Our Project Contracts

### AI-facing internal API

These are the only project endpoints that matter for the first AI integration:

- `GET /api/v1/agents`
- `GET /api/v1/agents/{id}`
- `POST /api/v1/agents/sessions`
- `GET /api/v1/agents/sessions/{id}`
- `POST /api/v1/agents/sessions/{id}/messages`
- `GET /api/v1/master-prompts`
- `GET /api/v1/master-prompts/{id}`
- `POST /api/v1/master-prompts`
- `PUT /api/v1/master-prompts/{id}`
- `POST /api/v1/master-prompts/{id}/activate`
- `POST /api/v1/master-prompts/{id}/archive`

### Files that define the contract

- [`backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/controller/AgentController.kt`](../backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/controller/AgentController.kt)
- [`backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/controller/MasterPromptController.kt`](../backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/controller/MasterPromptController.kt)
- [`backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/domain/port/AgentGatewayPort.kt`](../backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/domain/port/AgentGatewayPort.kt)
- [`backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/application/service/AgentService.kt`](../backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/application/service/AgentService.kt)
- [`backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/application/service/MasterPromptService.kt`](../backend/src/main/kotlin/ru/itech/sbertrack/platform/agent/application/service/MasterPromptService.kt)
- [`contracts/openapi/sbertrack-platform-openapi.yaml`](../contracts/openapi/sbertrack-platform-openapi.yaml)

## Minimal contract for the AI adapter

The adapter should only need:

- agent specialization;
- active master prompt text;
- conversation history;
- current user message;
- optional case title;
- optional artifact list.

The adapter should return:

- a single assistant text response;
- or a controlled fallback error that the service layer can convert into mock output.

## Scope guard

To keep the integration small:

- do not touch unrelated modules;
- do not redesign the domain model;
- do not add extra orchestration layers;
- do not make the frontend aware of provider internals;
- keep the API contract of `AgentService` stable.
