package ru.itech.sbertrack.platform.analytics.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.analytics.dto.response.AdminAnalyticsResponse
import ru.itech.sbertrack.platform.analytics.dto.response.ChartPointResponse
import ru.itech.sbertrack.platform.analytics.dto.response.CompetencyAnalyticsPointResponse
import ru.itech.sbertrack.platform.analytics.dto.response.CustomerAnalyticsResponse
import ru.itech.sbertrack.platform.analytics.dto.response.MetricResponse
import ru.itech.sbertrack.platform.analytics.dto.response.ModeratorAnalyticsResponse
import ru.itech.sbertrack.platform.analytics.dto.response.StudentAnalyticsResponse
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.cvbook.domain.model.PriorityStatus
import ru.itech.sbertrack.platform.cvbook.domain.port.CvBookDataPort
import ru.itech.sbertrack.platform.portfolio.domain.port.PortfolioDataPort
import ru.itech.sbertrack.platform.roadmap.domain.port.RoadmapDataPort
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import ru.itech.sbertrack.platform.track.domain.port.TrackDataPort
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.model.StudentType
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort

@Service
class AnalyticsService(
    private val userDataPort: UserDataPort,
    private val trackDataPort: TrackDataPort,
    private val caseDataPort: CaseDataPort,
    private val submissionDataPort: SubmissionDataPort,
    private val portfolioDataPort: PortfolioDataPort,
    private val cvBookDataPort: CvBookDataPort,
    private val agentDataPort: AgentDataPort,
    private val roadmapDataPort: RoadmapDataPort,
    private val authService: AuthService,
) {
    fun studentDashboard(authorization: String?): StudentAnalyticsResponse {
        val user = authService.currentUser(authorization)
        val portfolio = portfolioDataPort.findByStudentId(user.id)
        val submissions = submissionDataPort.list().filter { it.studentId == user.id }
        val roadmap = roadmapDataPort.findByStudentId(user.id)
        val nearestStep = roadmap?.steps?.firstOrNull { it.status.name == "IN_PROGRESS" || it.status.name == "AVAILABLE" }
        return StudentAnalyticsResponse(
            greeting = "Здравствуйте, ${user.fullName}",
            trajectoryTitle = roadmap?.title?.removePrefix("Дорожная карта: ") ?: "Маяк backend-разработки",
            currentLevel = "Уровень 2: системное проектирование",
            goal = "Стать маяком профессии",
            nearestCaseTitle = nearestStep?.title ?: "REST-контракты и OpenAPI",
            roadmapProgress = roadmap?.progressPercent ?: 24,
            metrics = listOf(
                MetricResponse("Завершено кейсов", portfolio?.completedCases?.size ?: 0, trend = 12),
                MetricResponse("В работе", submissions.count { it.status == SubmissionStatus.DRAFT || it.status == SubmissionStatus.SUBMITTED }),
                MetricResponse("Средний балл по обратной связи", submissions.flatMap { it.competencyScores.values }.ifEmpty { listOf(78) }.average().toInt(), "%", 4),
                MetricResponse("Прогресс траектории", roadmap?.progressPercent ?: 24, "%", 8),
                MetricResponse("Использовано ИИ-наставников", agentDataPort.listAgents().size),
                MetricResponse("Артефактов в портфолио", portfolio?.artifacts?.size ?: 0),
            ),
            completedCasesByDirection = listOf(
                ChartPointResponse("Инженерия", 3),
                ChartPointResponse("Продукт", 2),
                ChartPointResponse("Исследования", 1),
                ChartPointResponse("Финансы", 1),
            ),
            competencyRadar = competencyPoints(portfolio?.competencyProfile ?: Competency.entries.associateWith { 65 }),
            competencyGrowth = listOf(
                ChartPointResponse("Кейс 1", 58, 62),
                ChartPointResponse("Кейс 2", 66, 71),
                ChartPointResponse("Кейс 3", 74, 80),
                ChartPointResponse("Кейс 4", 82, 86),
            ),
            qualityDynamics = listOf(
                ChartPointResponse("Неделя 1", 62),
                ChartPointResponse("Неделя 2", 68),
                ChartPointResponse("Неделя 3", 74),
                ChartPointResponse("Неделя 4", 81),
                ChartPointResponse("Неделя 5", 84),
            ),
            funnel = listOf(
                ChartPointResponse("Выбран трек", 100),
                ChartPointResponse("Начат кейс", 82),
                ChartPointResponse("Отправлено решение", 61),
                ChartPointResponse("Получена обратная связь", 48),
                ChartPointResponse("Добавлено в портфолио", 42),
            ),
            recommendation = "Продолжите этап \"${nearestStep?.title ?: "REST-контракты и OpenAPI"}\": он усилит самостоятельность, архитектурное мышление и синергию с ИИ.",
        )
    }

    fun studentCompetencies(authorization: String?): List<CompetencyAnalyticsPointResponse> {
        val user = authService.currentUser(authorization)
        return competencyPoints(portfolioDataPort.findByStudentId(user.id)?.competencyProfile ?: Competency.entries.associateWith { 60 })
    }

    fun customerDashboard(): CustomerAnalyticsResponse {
        val submissions = submissionDataPort.list()
        val candidates = cvBookDataPort.list()
        return CustomerAnalyticsResponse(
            metrics = listOf(
                MetricResponse("Всего участников", userDataPort.list().count { it.role == UserRole.STUDENT }),
                MetricResponse("Активные участники", 34),
                MetricResponse("Решений отправлено", submissions.size),
                MetricResponse("Решений принято", submissions.count { it.status == SubmissionStatus.ACCEPTED }),
                MetricResponse("На доработке", submissions.count { it.status == SubmissionStatus.NEEDS_IMPROVEMENT }),
                MetricResponse("Приоритетных кандидатов", candidates.count { it.priorityStatus == PriorityStatus.PRIORITY }),
                MetricResponse("Средний уровень компетенций", 76, "%"),
            ),
            submissionStatuses = submissions.groupBy { it.status.name }.map { ChartPointResponse(it.key, it.value.size) },
            participantsByTrack = trackDataPort.list().mapIndexed { index, track -> ChartPointResponse(track.title, 18 + index * 7) },
            averageCompetencies = competencyPoints(Competency.entries.associateWith { 72 + it.ordinal * 3 }),
            activityDynamics = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб").mapIndexed { index, label -> ChartPointResponse(label, 8 + index * 4) },
            funnel = listOf(
                ChartPointResponse("Просмотр кейса", 140),
                ChartPointResponse("Старт", 96),
                ChartPointResponse("Отправка", 58),
                ChartPointResponse("Принятие", 32),
                ChartPointResponse("Приоритетный кандидат", 12),
            ),
        )
    }

    fun candidateAnalytics(): CustomerAnalyticsResponse = customerDashboard()

    fun moderatorDashboard(): ModeratorAnalyticsResponse =
        ModeratorAnalyticsResponse(
            metrics = listOf(
                MetricResponse("Кейсы на модерации", caseDataPort.list().count { it.status == CaseStatus.MODERATION }),
                MetricResponse("Активных наставников", agentDataPort.listAgents().size),
                MetricResponse("Сессий с наставниками", 128),
                MetricResponse("Средняя полезность", 86, "%"),
                MetricResponse("Обратной связи создано", 47),
                MetricResponse("Кейсов одобрено", caseDataPort.list().count { it.status == CaseStatus.PUBLISHED }),
                MetricResponse("Кейсов отклонено", 3),
            ),
            agentUsage = agentDataPort.listAgents().mapIndexed { index, agent -> ChartPointResponse(agent.name, 18 + index * 9) },
            agentUsefulness = agentDataPort.listAgents().mapIndexed { index, agent -> ChartPointResponse(agent.name, 78 + (index * 3) % 17) },
            recentSessions = listOf(
                "Иван Петров работал с Архитектором бэкенда",
                "Анна Смирнова уточнила исследование рынка",
                "Команда Domain First запросила проверочные вопросы",
            ),
        )

    fun adminDashboard(): AdminAnalyticsResponse {
        val users = userDataPort.list()
        return AdminAnalyticsResponse(
            metrics = listOf(
                MetricResponse("Пользователи", users.size),
                MetricResponse("Организации", users.mapNotNull { it.organizationName }.distinct().size),
                MetricResponse("Заказчики", users.count { it.role == UserRole.CUSTOMER }),
                MetricResponse("Студенты", users.count { it.studentType == StudentType.UNIVERSITY_STUDENT }),
                MetricResponse("Школьники", users.count { it.studentType == StudentType.SCHOOL_STUDENT }),
                MetricResponse("Модераторы", users.count { it.role == UserRole.MODERATOR }),
                MetricResponse("Активные кейсы", caseDataPort.list().count { it.status == CaseStatus.PUBLISHED }),
                MetricResponse("Отправленные решения", submissionDataPort.list().size),
            ),
            usersByRole = users.groupBy { it.role.name }.map { ChartPointResponse(it.key, it.value.size) },
            casesByStatus = caseDataPort.list().groupBy { it.status.name }.map { ChartPointResponse(it.key, it.value.size) },
            activityDynamics = listOf("Май", "Июнь", "Июль", "Август", "Сентябрь").mapIndexed { index, label -> ChartPointResponse(label, 36 + index * 18) },
            platformCompetencies = competencyPoints(Competency.entries.associateWith { 68 + it.ordinal * 4 }),
            participationFunnel = listOf(
                ChartPointResponse("Регистрация", 220),
                ChartPointResponse("Выбор траектории", 174),
                ChartPointResponse("Старт кейса", 121),
                ChartPointResponse("Отправка", 73),
                ChartPointResponse("Портфолио", 51),
            ),
        )
    }

    private fun competencyPoints(values: Map<Competency, Int>): List<CompetencyAnalyticsPointResponse> =
        Competency.entries.map { competency ->
            CompetencyAnalyticsPointResponse(
                competency = competency,
                value = values[competency] ?: 0,
                previousValue = ((values[competency] ?: 0) - 8).coerceAtLeast(0),
            )
        }
}
