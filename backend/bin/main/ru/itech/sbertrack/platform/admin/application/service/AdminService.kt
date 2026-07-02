package ru.itech.sbertrack.platform.admin.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.admin.dto.response.AdminDashboardResponse
import ru.itech.sbertrack.platform.admin.dto.response.AdminStatisticsResponse
import ru.itech.sbertrack.platform.agent.application.mapper.AgentMapper
import ru.itech.sbertrack.platform.agent.domain.model.AgentStatus
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.challengecase.application.mapper.CaseMapper
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.cvbook.domain.port.CvBookDataPort
import ru.itech.sbertrack.platform.submission.application.mapper.SubmissionMapper
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import ru.itech.sbertrack.platform.track.application.mapper.TrackMapper
import ru.itech.sbertrack.platform.track.domain.port.TrackDataPort
import ru.itech.sbertrack.platform.user.application.mapper.UserMapper
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort

@Service
class AdminService(
    private val userDataPort: UserDataPort,
    private val trackDataPort: TrackDataPort,
    private val caseDataPort: CaseDataPort,
    private val submissionDataPort: SubmissionDataPort,
    private val cvBookDataPort: CvBookDataPort,
    private val agentDataPort: AgentDataPort,
    private val userMapper: UserMapper,
    private val trackMapper: TrackMapper,
    private val caseMapper: CaseMapper,
    private val submissionMapper: SubmissionMapper,
    private val agentMapper: AgentMapper,
) {
    fun statistics(): AdminStatisticsResponse =
        AdminStatisticsResponse(
            usersCount = userDataPort.list().size,
            tracksCount = trackDataPort.list().size,
            casesCount = caseDataPort.list().size,
            submissionsCount = submissionDataPort.list().size,
            cvBookCandidatesCount = cvBookDataPort.list().size,
            activeAgentsCount = agentDataPort.listAgents().count { it.status == AgentStatus.ACTIVE },
            competencyDistribution = competencyDistribution(),
        )

    fun dashboard(): AdminDashboardResponse =
        AdminDashboardResponse(
            statistics = statistics(),
            users = userDataPort.list().map(userMapper::toResponse),
            tracks = trackDataPort.list().map(trackMapper::toResponse),
            cases = caseDataPort.list().map(caseMapper::toResponse),
            submissions = submissionDataPort.list().map(submissionMapper::toResponse),
            agents = agentDataPort.listAgents().map(agentMapper::toResponse),
        )

    private fun competencyDistribution(): Map<Competency, Int> {
        val cases = caseDataPort.list()
        return Competency.entries.associateWith { competency ->
            cases.map { it.competencyWeights[competency] ?: 0 }.ifEmpty { listOf(0) }.average().toInt()
        }
    }
}
