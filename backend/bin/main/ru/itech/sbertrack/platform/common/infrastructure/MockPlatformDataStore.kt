package ru.itech.sbertrack.platform.common.infrastructure

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.agent.domain.model.AgentCapability
import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentSpecialization
import ru.itech.sbertrack.platform.agent.domain.model.AgentStatus
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt
import ru.itech.sbertrack.platform.agent.domain.model.MasterPromptStatus
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.domain.model.FeedbackMode
import ru.itech.sbertrack.platform.challengecase.domain.model.PracticalCase
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import ru.itech.sbertrack.platform.cvbook.domain.model.CvBookCandidate
import ru.itech.sbertrack.platform.cvbook.domain.model.PriorityStatus
import ru.itech.sbertrack.platform.feedback.domain.model.Feedback
import ru.itech.sbertrack.platform.feedback.domain.model.FeedbackAuthorType
import ru.itech.sbertrack.platform.portfolio.domain.model.Portfolio
import ru.itech.sbertrack.platform.reflection.domain.model.Reflection
import ru.itech.sbertrack.platform.roadmap.domain.model.RoadmapStep
import ru.itech.sbertrack.platform.roadmap.domain.model.StudentRoadmap
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import ru.itech.sbertrack.platform.track.domain.model.Track
import ru.itech.sbertrack.platform.track.domain.model.TrackStatus
import ru.itech.sbertrack.platform.trajectory.domain.model.Trajectory
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNode
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNodeStatus
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNodeType
import ru.itech.sbertrack.platform.user.domain.model.StudentType
import ru.itech.sbertrack.platform.user.domain.model.User
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class MockPlatformDataStore {
    val users: ConcurrentHashMap<UUID, User> = ConcurrentHashMap()
    val passwordsByEmail: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    val tracks: ConcurrentHashMap<UUID, Track> = ConcurrentHashMap()
    val cases: ConcurrentHashMap<UUID, PracticalCase> = ConcurrentHashMap()
    val submissions: ConcurrentHashMap<UUID, Submission> = ConcurrentHashMap()
    val portfolios: ConcurrentHashMap<UUID, Portfolio> = ConcurrentHashMap()
    val cvBookCandidates: ConcurrentHashMap<UUID, CvBookCandidate> = ConcurrentHashMap()
    val agents: ConcurrentHashMap<UUID, AgentDefinition> = ConcurrentHashMap()
    val masterPrompts: ConcurrentHashMap<UUID, MasterPrompt> = ConcurrentHashMap()
    val agentSessions: ConcurrentHashMap<UUID, AgentSession> = ConcurrentHashMap()
    val feedback: ConcurrentHashMap<UUID, Feedback> = ConcurrentHashMap()
    val reflections: ConcurrentHashMap<UUID, Reflection> = ConcurrentHashMap()
    val trajectories: ConcurrentHashMap<UUID, Trajectory> = ConcurrentHashMap()
    val trajectoryNodes: ConcurrentHashMap<UUID, TrajectoryNode> = ConcurrentHashMap()
    val roadmaps: ConcurrentHashMap<UUID, StudentRoadmap> = ConcurrentHashMap()

    init {
        val studentId = id("user-student")
        val schoolStudentId = id("user-school")
        val customerId = id("user-customer-sber")
        val partnerId = id("user-customer-partner")
        val moderatorId = id("user-moderator")
        val adminId = id("user-admin")

        val seededUsers = listOf(
            User(
                id = studentId,
                fullName = "Иван Петров",
                email = "student@example.com",
                role = UserRole.STUDENT,
                organizationName = "ИТМО",
                studentType = StudentType.UNIVERSITY_STUDENT,
                createdAt = instant("2026-01-15T09:00:00Z"),
            ),
            User(
                id = schoolStudentId,
                fullName = "Анна Смирнова",
                email = "school@example.com",
                role = UserRole.STUDENT,
                organizationName = "Школа 1535",
                studentType = StudentType.SCHOOL_STUDENT,
                createdAt = instant("2026-02-02T09:00:00Z"),
            ),
            User(
                id = customerId,
                fullName = "Мария Соколова",
                email = "customer@example.com",
                role = UserRole.CUSTOMER,
                organizationName = "Сбер",
                createdAt = instant("2026-01-20T09:00:00Z"),
            ),
            User(
                id = partnerId,
                fullName = "Алексей Орлов",
                email = "partner@example.com",
                role = UserRole.CUSTOMER,
                organizationName = "Индустриальный партнёр",
                createdAt = instant("2026-01-25T09:00:00Z"),
            ),
            User(
                id = moderatorId,
                fullName = "Модератор платформы",
                email = "moderator@example.com",
                role = UserRole.MODERATOR,
                createdAt = instant("2026-01-05T09:00:00Z"),
            ),
            User(
                id = adminId,
                fullName = "Администратор",
                email = "admin@example.com",
                role = UserRole.ADMIN,
                createdAt = instant("2026-01-01T09:00:00Z"),
            ),
        )
        users.putAll(seededUsers.associateBy { it.id })
        passwordsByEmail.putAll(seededUsers.associate { it.email.lowercase() to "password" })

        val trackEducationId = id("track-ai-education")
        val trackFinanceId = id("track-finance")
        val trackBackendId = id("track-backend")
        val trackCityId = id("track-city")
        val trackResearchId = id("track-school-research")

        val caseBackendId = id("case-backend-platform")
        val caseFinanceId = id("case-financial-model")
        val caseEducationHypothesisId = id("case-education-hypothesis")
        val caseAgentId = id("case-agent-mentor")
        val caseCvBookId = id("case-cvbook-service")
        val caseMarketId = id("case-career-market")

        val seededCases = listOf(
            PracticalCase(
                id = caseBackendId,
                trackId = trackBackendId,
                title = "Спроектировать backend для платформы практических кейсов",
                shortDescription = "Разложить платформу на доменные модули, контракты и порты.",
                fullDescription = "Нужно подготовить backend-архитектуру MVP: доменная модель, REST API, in-memory адаптеры, Swagger и сценарии дальнейшего подключения persistence.",
                customerId = customerId,
                customerName = "Сбер",
                status = CaseStatus.PUBLISHED,
                difficulty = Difficulty.ADVANCED,
                participantLimit = 20,
                expectedResult = "Архитектурная схема, список API-контрактов, описание портов и демонстрационная реализация ключевых сценариев.",
                feedbackMode = FeedbackMode.MIXED,
                competencyWeights = weights(25, 20, 10, 25, 20),
                tags = listOf("backend", "architecture", "api"),
                deadline = LocalDate.of(2026, 9, 15),
                createdAt = instant("2026-05-01T08:30:00Z"),
            ),
            PracticalCase(
                id = caseFinanceId,
                trackId = trackFinanceId,
                title = "Собрать финансовую модель MVP",
                shortDescription = "Описать unit-экономику и сценарии устойчивости MVP.",
                fullDescription = "Команда должна построить простую финансовую модель без платежной интеграции: гипотезы, драйверы затрат, сценарии и чувствительность.",
                customerId = customerId,
                customerName = "Сбер",
                status = CaseStatus.PUBLISHED,
                difficulty = Difficulty.INTERMEDIATE,
                participantLimit = 30,
                expectedResult = "Таблица финансовой модели, список гипотез и краткий вывод по рискам.",
                feedbackMode = FeedbackMode.LIVE,
                competencyWeights = weights(20, 20, 10, 10, 40),
                tags = listOf("finance", "mvp", "unit-economics"),
                deadline = LocalDate.of(2026, 8, 30),
                createdAt = instant("2026-05-03T08:30:00Z"),
            ),
            PracticalCase(
                id = caseEducationHypothesisId,
                trackId = trackEducationId,
                title = "Проверить гипотезу образовательного продукта",
                shortDescription = "Сформулировать и проверить гипотезу пользы для студентов.",
                fullDescription = "Нужно описать проблему, сегмент, способ проверки гипотезы, метрики и план интервью с пользователями.",
                customerId = partnerId,
                customerName = "Индустриальный партнёр",
                status = CaseStatus.PUBLISHED,
                difficulty = Difficulty.INTERMEDIATE,
                participantLimit = 25,
                expectedResult = "Lean Canvas, план проверки и краткий отчёт по результатам.",
                feedbackMode = FeedbackMode.AGENT,
                competencyWeights = weights(15, 25, 15, 20, 25),
                tags = listOf("product", "education", "hypothesis"),
                deadline = LocalDate.of(2026, 8, 20),
                createdAt = instant("2026-05-05T08:30:00Z"),
            ),
            PracticalCase(
                id = caseAgentId,
                trackId = trackEducationId,
                title = "Разработать концепцию ИИ-наставника",
                shortDescription = "Описать ограничения наставника, тон ответа и формат помощи.",
                fullDescription = "ИИ-наставник должен помогать структурировать работу, задавать вопросы и не выполнять финальное решение за участника.",
                customerId = customerId,
                customerName = "Сбер",
                status = CaseStatus.PUBLISHED,
                difficulty = Difficulty.ADVANCED,
                participantLimit = 18,
                expectedResult = "Концепция наставника, контракт взаимодействия и примеры безопасных ответов.",
                feedbackMode = FeedbackMode.MIXED,
                competencyWeights = weights(20, 20, 10, 40, 10),
                tags = listOf("ИИ-наставник", "промпт", "наставник"),
                deadline = LocalDate.of(2026, 9, 5),
                createdAt = instant("2026-05-07T08:30:00Z"),
            ),
            PracticalCase(
                id = caseCvBookId,
                trackId = trackBackendId,
                title = "Спроектировать сервис витрины кандидатов",
                shortDescription = "Предложить сервис витрины приоритетных кандидатов.",
                fullDescription = "Нужно описать доменную модель кандидата, фильтры, приоритетный статус и связь с портфолио.",
                customerId = customerId,
                customerName = "Сбер",
                status = CaseStatus.MODERATION,
                difficulty = Difficulty.INTERMEDIATE,
                participantLimit = 22,
                expectedResult = "Контракт API витрины кандидатов, макет карточки кандидата и правила обновления статусов.",
                feedbackMode = FeedbackMode.LIVE,
                competencyWeights = weights(20, 20, 20, 10, 30),
                tags = listOf("витрина кандидатов", "карьера", "портфолио"),
                deadline = LocalDate.of(2026, 9, 10),
                createdAt = instant("2026-05-09T08:30:00Z"),
            ),
            PracticalCase(
                id = caseMarketId,
                trackId = trackResearchId,
                title = "Провести анализ рынка карьерных платформ",
                shortDescription = "Сравнить карьерные платформы и выделить незакрытые потребности.",
                fullDescription = "Школьникам нужно собрать открытые данные, выделить сегменты, сравнить продукты и сформулировать выводы.",
                customerId = partnerId,
                customerName = "Индустриальный партнёр",
                status = CaseStatus.PUBLISHED,
                difficulty = Difficulty.BEGINNER,
                participantLimit = 40,
                expectedResult = "Карта конкурентов, таблица сравнения и список продуктовых возможностей.",
                feedbackMode = FeedbackMode.AGENT,
                competencyWeights = weights(25, 25, 10, 15, 25),
                tags = listOf("research", "career", "market"),
                deadline = LocalDate.of(2026, 8, 25),
                createdAt = instant("2026-05-11T08:30:00Z"),
            ),
        )
        cases.putAll(seededCases.associateBy { it.id })

        val seededTracks = listOf(
            Track(
                id = trackEducationId,
                title = "ИИ-продукты для образования",
                description = "Продуктовые и агентные решения для практического обучения.",
                customerId = customerId,
                customerName = "Сбер",
                difficulty = Difficulty.ADVANCED,
                status = TrackStatus.ACTIVE,
                targetAudience = "Студенты старших курсов",
                caseIds = listOf(caseEducationHypothesisId, caseAgentId),
                createdAt = instant("2026-04-01T08:00:00Z"),
            ),
            Track(
                id = trackFinanceId,
                title = "Финансовая аналитика и бизнес-модели",
                description = "Unit-экономика, сценарии MVP и бизнес-гипотезы.",
                customerId = customerId,
                customerName = "Сбер",
                difficulty = Difficulty.INTERMEDIATE,
                status = TrackStatus.ACTIVE,
                targetAudience = "Студенты экономики и продуктовые команды",
                caseIds = listOf(caseFinanceId),
                createdAt = instant("2026-04-02T08:00:00Z"),
            ),
            Track(
                id = trackBackendId,
                title = "Backend-платформа для промышленного сервиса",
                description = "Контракты, доменные модули и сервисная архитектура.",
                customerId = customerId,
                customerName = "Сбер",
                difficulty = Difficulty.ADVANCED,
                status = TrackStatus.ACTIVE,
                targetAudience = "Backend-разработчики и системные аналитики",
                caseIds = listOf(caseBackendId, caseCvBookId),
                createdAt = instant("2026-04-03T08:00:00Z"),
            ),
            Track(
                id = trackCityId,
                title = "Городские цифровые сервисы",
                description = "Гипотезы сервисов для городской среды и B2G-процессов.",
                customerId = partnerId,
                customerName = "Индустриальный партнёр",
                difficulty = Difficulty.INTERMEDIATE,
                status = TrackStatus.ACTIVE,
                targetAudience = "Студенты проектных программ",
                caseIds = emptyList(),
                createdAt = instant("2026-04-04T08:00:00Z"),
            ),
            Track(
                id = trackResearchId,
                title = "Исследовательский трек для школьников",
                description = "Исследовательские задачи с акцентом на структуру и самостоятельность.",
                customerId = partnerId,
                customerName = "Индустриальный партнёр",
                difficulty = Difficulty.BEGINNER,
                status = TrackStatus.ACTIVE,
                targetAudience = "Школьники 9-11 классов",
                caseIds = listOf(caseMarketId),
                createdAt = instant("2026-04-05T08:00:00Z"),
            ),
        )
        tracks.putAll(seededTracks.associateBy { it.id })

        val submissionId = id("submission-backend-ivan")
        val schoolSubmissionId = id("submission-market-school")
        val seededSubmissions = listOf(
            Submission(
                id = submissionId,
                caseId = caseBackendId,
                studentId = studentId,
                teamName = "Команда Domain First",
                title = "Backend-архитектура платформы кейсов",
                description = "Предложены доменные модули, REST API, in-memory адаптеры и схема будущей интеграции.",
                artifactUrl = "Архитектурная схема backend.pdf",
                status = SubmissionStatus.ACCEPTED,
                competencyScores = weights(86, 80, 70, 84, 78),
                submittedAt = instant("2026-06-12T12:00:00Z"),
            ),
            Submission(
                id = schoolSubmissionId,
                caseId = caseMarketId,
                studentId = schoolStudentId,
                teamName = "Career Research",
                title = "Анализ рынка карьерных платформ",
                description = "Собрана карта конкурентов и выделены возможности для витрины кандидатов.",
                artifactUrl = "Анализ карьерных платформ.xlsx",
                status = SubmissionStatus.SUBMITTED,
                competencyScores = weights(76, 82, 65, 70, 78),
                submittedAt = instant("2026-06-18T10:20:00Z"),
            ),
        )
        submissions.putAll(seededSubmissions.associateBy { it.id })

        val feedbackId = id("feedback-backend-ivan")
        val seededFeedback = listOf(
            Feedback(
                id = feedbackId,
                submissionId = submissionId,
                authorType = FeedbackAuthorType.CUSTOMER,
                authorName = "Мария Соколова",
                text = "Хорошо выделены границы модулей и будущие порты интеграции. Следующий шаг - уточнить права ролей в критичных сценариях.",
                recommendations = listOf(
                    "Добавить матрицу прав для CUSTOMER и MODERATOR",
                    "Разделить DTO создания и ответа для сложных сущностей",
                    "Показать, где появится persistence adapter",
                ),
                competencyDelta = weights(4, 3, 2, 4, 3),
                createdAt = instant("2026-06-13T14:30:00Z"),
            ),
        )
        feedback.putAll(seededFeedback.associateBy { it.id })
        submissions[submissionId] = submissions.getValue(submissionId).addFeedback(feedbackId, SubmissionStatus.ACCEPTED)

        val seededPortfolios = listOf(
            Portfolio(
                id = id("portfolio-ivan"),
                studentId = studentId,
                summary = "Backend-разработчик с фокусом на доменное моделирование, API-контракты и осознанное использование ИИ-наставников.",
                completedCases = listOf("Спроектировать backend для платформы практических кейсов"),
                competencyProfile = weights(78, 74, 66, 80, 72),
                artifacts = listOf("Архитектурная схема backend.pdf"),
                feedbackHighlights = listOf("Сильная декомпозиция модулей и понятная граница in-memory интеграций."),
                cvBookIncluded = true,
            ),
            Portfolio(
                id = id("portfolio-anna"),
                studentId = schoolStudentId,
                summary = "Начинающий исследователь: умеет собирать факты, сравнивать продукты и формулировать выводы.",
                completedCases = listOf("Провести анализ рынка карьерных платформ"),
                competencyProfile = weights(70, 76, 64, 68, 74),
                artifacts = listOf("Анализ карьерных платформ.xlsx"),
                feedbackHighlights = listOf("Хорошо структурирован конкурентный анализ."),
                cvBookIncluded = true,
            ),
        )
        portfolios.putAll(seededPortfolios.associateBy { it.studentId })

        val seededCandidates = listOf(
            CvBookCandidate(
                id = id("cvbook-ivan"),
                studentId = studentId,
                fullName = "Иван Петров",
                organizationName = "ИТМО",
                completedCasesCount = 1,
                averageScore = 80,
                competencyProfile = weights(78, 74, 66, 80, 72),
                tags = listOf("backend", "архитектура", "синергия с ИИ"),
                priorityStatus = PriorityStatus.PRIORITY,
                lastActivityAt = instant("2026-06-13T14:30:00Z"),
            ),
            CvBookCandidate(
                id = id("cvbook-anna"),
                studentId = schoolStudentId,
                fullName = "Анна Смирнова",
                organizationName = "Школа 1535",
                completedCasesCount = 1,
                averageScore = 74,
                competencyProfile = weights(70, 76, 64, 68, 74),
                tags = listOf("research", "market", "career"),
                priorityStatus = PriorityStatus.REGULAR,
                lastActivityAt = instant("2026-06-18T10:20:00Z"),
            ),
        )
        cvBookCandidates.putAll(seededCandidates.associateBy { it.id })

        val agentSeed = seedAgents(moderatorId)
        agents.putAll(agentSeed.first.associateBy { it.id })
        masterPrompts.putAll(agentSeed.second.associateBy { it.id })

        val trajectorySeed = seedTrajectories(
            trackBackendId = trackBackendId,
            trackEducationId = trackEducationId,
            trackFinanceId = trackFinanceId,
            trackResearchId = trackResearchId,
            caseBackendId = caseBackendId,
            caseFinanceId = caseFinanceId,
            caseEducationHypothesisId = caseEducationHypothesisId,
            caseAgentId = caseAgentId,
            caseCvBookId = caseCvBookId,
            caseMarketId = caseMarketId,
        )
        trajectories.putAll(trajectorySeed.first.associateBy { it.id })
        trajectoryNodes.putAll(trajectorySeed.second.associateBy { it.id })
        roadmaps.putAll(seedRoadmaps(studentId, trajectorySeed.first.first(), trajectorySeed.second).associateBy { it.id })

        val seededReflections = listOf(
            Reflection(
                id = id("reflection-ivan-backend"),
                submissionId = submissionId,
                studentId = studentId,
                answers = mapOf(
                    "Что получилось?" to "Удалось выделить границы модулей и API.",
                    "Что было сложно?" to "Согласовать роли и статусы кейсов.",
                    "Как использовался ИИ?" to "Как помощник для проверки полноты сценариев.",
                    "Что сделал сам?" to "Финальную архитектуру и описание портов.",
                    "Что улучшить?" to "Добавить больше тестов на переходы статусов.",
                ),
                summary = "ИИ помог структурировать вопросы, но итоговое решение оформлялось самостоятельно.",
                createdAt = instant("2026-06-14T09:00:00Z"),
            ),
        )
        reflections.putAll(seededReflections.associateBy { it.submissionId })
    }

    fun id(seed: String): UUID =
        UUID.nameUUIDFromBytes("sbertrack-platform:$seed".toByteArray(StandardCharsets.UTF_8))

    private fun instant(value: String): Instant = Instant.parse(value)

    private fun weights(
        abstractThinking: Int,
        autonomy: Int,
        collaboration: Int,
        humanAiSynergy: Int,
        hypothesisAndProduct: Int,
    ): Map<Competency, Int> = mapOf(
        Competency.ABSTRACT_THINKING to abstractThinking,
        Competency.AUTONOMY to autonomy,
        Competency.COLLABORATION to collaboration,
        Competency.HUMAN_AI_SYNERGY to humanAiSynergy,
        Competency.HYPOTHESIS_AND_PRODUCT_THINKING to hypothesisAndProduct,
    )

    private fun seedAgents(moderatorId: UUID): Pair<List<AgentDefinition>, List<MasterPrompt>> {
        val backendAgentId = id("agent-backend")
        val frontendAgentId = id("agent-frontend")
        val productAgentId = id("agent-product")
        val financeAgentId = id("agent-finance")
        val researchAgentId = id("agent-research")
        val reflectionAgentId = id("agent-reflection")
        val feedbackAgentId = id("agent-feedback")
        val promptIds = listOf(
            id("prompt-backend"),
            id("prompt-frontend"),
            id("prompt-product"),
            id("prompt-finance"),
            id("prompt-research"),
            id("prompt-reflection"),
            id("prompt-feedback"),
        )
        val prompts = listOf(
            MasterPrompt(promptIds[0], backendAgentId, "Шаблон архитектора бэкенда", "Помогай проектировать backend через вопросы, границы модулей и контракты.", 1, MasterPromptStatus.ACTIVE, moderatorId),
            MasterPrompt(promptIds[1], frontendAgentId, "Шаблон наставника интерфейсов", "Помогай проектировать интерфейс, Material Design, навигацию, формы и дашборды.", 1, MasterPromptStatus.ACTIVE, moderatorId),
            MasterPrompt(promptIds[2], productAgentId, "Шаблон продуктового наставника", "Помогай формулировать гипотезы, метрики и план проверки.", 1, MasterPromptStatus.ACTIVE, moderatorId),
            MasterPrompt(promptIds[3], financeAgentId, "Шаблон финансовой модели", "Помогай строить модель через драйверы, сценарии и проверки предположений.", 1, MasterPromptStatus.ACTIVE, moderatorId),
            MasterPrompt(promptIds[4], researchAgentId, "Шаблон исследовательского наставника", "Помогай структурировать исследование, источники и выводы.", 1, MasterPromptStatus.ACTIVE, moderatorId),
            MasterPrompt(promptIds[5], reflectionAgentId, "Шаблон рефлексии", "Помогай участнику провести честную рефлексию собственного вклада.", 1, MasterPromptStatus.ACTIVE, moderatorId),
            MasterPrompt(promptIds[6], feedbackAgentId, "Шаблон обратной связи", "Помогай формировать развивающую обратную связь без оценки личности.", 1, MasterPromptStatus.ACTIVE, moderatorId),
        )
        val sharedCapabilities = listOf(
            AgentCapability.STRUCTURE_SOLUTION,
            AgentCapability.ASK_CHECK_QUESTIONS,
            AgentCapability.SUGGEST_NEXT_STEPS,
        )
        val agents = listOf(
            AgentDefinition(backendAgentId, "backend-architect", "Архитектор бэкенда", "Помогает проектировать сервисы, контракты, роли, API и структуру backend.", AgentSpecialization.BACKEND_ARCHITECTURE, AgentStatus.ACTIVE, promptIds[0], sharedCapabilities + AgentCapability.REVIEW_ASSUMPTIONS),
            AgentDefinition(frontendAgentId, "frontend-mentor", "Наставник frontend-разработки", "Помогает продумать интерфейс, Material Design, навигацию, формы и дашборды.", AgentSpecialization.FRONTEND_INTERFACE, AgentStatus.ACTIVE, promptIds[1], sharedCapabilities + AgentCapability.REVIEW_ASSUMPTIONS),
            AgentDefinition(productAgentId, "product-hypothesis", "Продуктовый наставник", "Помогает формулировать гипотезы, ценность, аудиторию и метрики.", AgentSpecialization.PRODUCT_HYPOTHESIS, AgentStatus.ACTIVE, promptIds[2], sharedCapabilities),
            AgentDefinition(financeAgentId, "financial-model", "Наставник финансовой модели", "Помогает структурировать расходы, доходы, unit-экономику и сценарии.", AgentSpecialization.FINANCIAL_MODELING, AgentStatus.ACTIVE, promptIds[3], sharedCapabilities + AgentCapability.REVIEW_ASSUMPTIONS),
            AgentDefinition(researchAgentId, "research", "Исследовательский наставник", "Помогает анализировать рынок, аналоги, конкурентов и источники.", AgentSpecialization.MARKET_RESEARCH, AgentStatus.ACTIVE, promptIds[4], sharedCapabilities),
            AgentDefinition(reflectionAgentId, "reflection", "Наставник рефлексии", "Помогает разобрать, что получилось, что было сложно и что улучшить.", AgentSpecialization.REFLECTION, AgentStatus.ACTIVE, promptIds[5], listOf(AgentCapability.ASK_CHECK_QUESTIONS, AgentCapability.SUGGEST_NEXT_STEPS)),
            AgentDefinition(feedbackAgentId, "feedback", "Наставник обратной связи", "Помогает понять сильные и слабые стороны решения.", AgentSpecialization.FEEDBACK, AgentStatus.ACTIVE, promptIds[6], sharedCapabilities + AgentCapability.FORM_FEEDBACK),
        )
        return agents to prompts
    }

    private fun seedTrajectories(
        trackBackendId: UUID,
        trackEducationId: UUID,
        trackFinanceId: UUID,
        trackResearchId: UUID,
        caseBackendId: UUID,
        caseFinanceId: UUID,
        caseEducationHypothesisId: UUID,
        caseAgentId: UUID,
        caseCvBookId: UUID,
        caseMarketId: UUID,
    ): Pair<List<Trajectory>, List<TrajectoryNode>> {
        val specs = listOf(
            TrajectorySpec(
                seed = "trajectory-backend",
                title = "Маяк backend-разработки",
                description = "Путь от проектирования сервисов к промышленному backend-кейсу.",
                targetDescription = "Специалист, который уверенно проектирует сервисы, API, роли, данные и наблюдаемость.",
                direction = "Инженерия",
                difficulty = Difficulty.ADVANCED,
                duration = 14,
                trackId = trackBackendId,
                caseIds = listOf(caseBackendId, caseBackendId, caseCvBookId, caseBackendId),
                stages = listOf("Основы проектирования сервисов", "REST-контракты и OpenAPI", "Авторизация и роли", "Работа с данными", "Очереди и интеграции", "Наблюдаемость", "Финальный промышленный кейс"),
            ),
            TrajectorySpec(
                seed = "trajectory-frontend",
                title = "Маяк frontend-разработки",
                description = "Путь к продуктовой витрине с Material Design, формами, таблицами и дашбордами.",
                targetDescription = "Специалист, который собирает понятные интерфейсы для сложных enterprise-сценариев.",
                direction = "Интерфейсы",
                difficulty = Difficulty.INTERMEDIATE,
                duration = 12,
                trackId = trackEducationId,
                caseIds = listOf(caseAgentId, caseEducationHypothesisId, caseAgentId, caseCvBookId),
                stages = listOf("UI-композиция", "Material Design", "Формы и таблицы", "Ролевая навигация", "Дашборды и графики", "Интеграция с API", "Финальная витрина продукта"),
            ),
            TrajectorySpec(
                seed = "trajectory-product",
                title = "Маяк продуктового мышления",
                description = "Путь от сегмента и боли пользователя до продуктовой защиты.",
                targetDescription = "Специалист, который проверяет гипотезы и доводит решение до ценности для пользователя.",
                direction = "Продукт",
                difficulty = Difficulty.INTERMEDIATE,
                duration = 10,
                trackId = trackEducationId,
                caseIds = listOf(caseEducationHypothesisId, caseAgentId, caseMarketId, caseEducationHypothesisId),
                stages = listOf("Сегменты пользователей", "Боли и потребности", "Гипотеза ценности", "MVP", "Метрики", "Проверка гипотезы", "Продуктовая защита"),
            ),
            TrajectorySpec(
                seed = "trajectory-finance",
                title = "Маяк финансовой аналитики",
                description = "Путь от структуры модели к защите финансовых сценариев.",
                targetDescription = "Специалист, который видит экономику решения и умеет защищать модель роста.",
                direction = "Финансы",
                difficulty = Difficulty.INTERMEDIATE,
                duration = 9,
                trackId = trackFinanceId,
                caseIds = listOf(caseFinanceId, caseFinanceId, caseMarketId, caseFinanceId),
                stages = listOf("Структура доходов и расходов", "Unit-экономика", "Сценарии роста", "Риски", "Финансовая модель", "Защита модели"),
            ),
        )
        val trajectories = mutableListOf<Trajectory>()
        val nodes = mutableListOf<TrajectoryNode>()
        specs.forEachIndexed { trajectoryIndex, spec ->
            val trajectoryId = id(spec.seed)
            val nodeIds = spec.stages.mapIndexed { index, _ -> id("${spec.seed}-node-$index") }
            val trajectoryNodes = spec.stages.mapIndexed { index, stage ->
                TrajectoryNode(
                    id = nodeIds[index],
                    trajectoryId = trajectoryId,
                    title = if (index == spec.stages.lastIndex) "Маяк профессии" else stage,
                    description = if (index == spec.stages.lastIndex) spec.targetDescription else "Этап траектории: $stage.",
                    type = when (index) {
                        0 -> TrajectoryNodeType.START
                        spec.stages.lastIndex -> TrajectoryNodeType.FINAL_PROJECT
                        1 -> TrajectoryNodeType.TRACK
                        3 -> TrajectoryNodeType.CHECKPOINT
                        5 -> TrajectoryNodeType.MENTOR_REVIEW
                        else -> TrajectoryNodeType.CASE
                    },
                    positionX = 80 + index * 190,
                    positionY = 80 + trajectoryIndex * 150 + if (index % 2 == 0) 0 else 58,
                    trackId = if (index == 1) spec.trackId else null,
                    caseId = spec.caseIds.getOrNull(index - 2),
                    requiredCompetencies = when (spec.direction) {
                        "Инженерия" -> weights(28, 22, 10, 25, 15)
                        "Интерфейсы" -> weights(18, 18, 25, 22, 17)
                        "Продукт" -> weights(18, 24, 18, 15, 25)
                        else -> weights(22, 22, 10, 10, 36)
                    },
                    status = when (index) {
                        0 -> TrajectoryNodeStatus.COMPLETED
                        1 -> TrajectoryNodeStatus.IN_PROGRESS
                        2 -> TrajectoryNodeStatus.AVAILABLE
                        else -> TrajectoryNodeStatus.LOCKED
                    },
                    nextNodeIds = nodeIds.getOrNull(index + 1)?.let { listOf(it) } ?: emptyList(),
                )
            }
            nodes += trajectoryNodes
            trajectories += Trajectory(
                id = trajectoryId,
                title = spec.title,
                description = spec.description,
                targetRoleTitle = spec.title,
                targetRoleDescription = spec.targetDescription,
                direction = spec.direction,
                difficulty = spec.difficulty,
                estimatedDurationWeeks = spec.duration,
                nodeIds = nodeIds,
                createdAt = instant("2026-05-${(trajectoryIndex + 12).toString().padStart(2, '0')}T08:00:00Z"),
            )
        }
        return trajectories to nodes
    }

    private fun seedRoadmaps(studentId: UUID, trajectory: Trajectory, nodes: List<TrajectoryNode>): List<StudentRoadmap> {
        val roadmapId = id("roadmap-ivan-backend")
        val trajectoryNodes = nodes.filter { it.trajectoryId == trajectory.id }
            .sortedWith(compareBy<TrajectoryNode> { it.positionX }.thenBy { it.positionY })
        val steps = trajectoryNodes.mapIndexed { index, node ->
            RoadmapStep(
                id = id("roadmap-ivan-backend-step-$index"),
                roadmapId = roadmapId,
                nodeId = node.id,
                title = node.title,
                description = node.description,
                status = node.status,
                caseId = node.caseId,
                orderIndex = index,
            )
        }
        return listOf(
            StudentRoadmap(
                id = roadmapId,
                studentId = studentId,
                trajectoryId = trajectory.id,
                title = "Дорожная карта: ${trajectory.title}",
                currentNodeId = steps.first { it.status == TrajectoryNodeStatus.IN_PROGRESS }.nodeId,
                progressPercent = 24,
                selectedAt = instant("2026-06-01T09:00:00Z"),
                expectedFinishDate = LocalDate.of(2026, 10, 10),
                steps = steps,
            ),
        )
    }

    private data class TrajectorySpec(
        val seed: String,
        val title: String,
        val description: String,
        val targetDescription: String,
        val direction: String,
        val difficulty: Difficulty,
        val duration: Int,
        val trackId: UUID,
        val caseIds: List<UUID>,
        val stages: List<String>,
    )
}
