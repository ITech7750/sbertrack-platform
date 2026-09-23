package ru.itech.sbertrack.platform.submission.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "submissions")
class SubmissionEntity(
    @Id
    val id: UUID,
    @Column(name = "case_id", nullable = false)
    val caseId: UUID,
    @Column(name = "student_id", nullable = false)
    val studentId: UUID,
    @Column(name = "team_name")
    val teamName: String?,
    @Column(nullable = false)
    val title: String,
    @Column(nullable = false)
    val description: String,
    @Column(name = "artifact_url")
    val artifactUrl: String?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    val status: SubmissionStatus,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "competency_scores", nullable = false)
    val competencyScores: Map<Competency, Int>,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback_ids", nullable = false)
    val feedbackIds: List<UUID>,
    @Column(name = "reflection_id")
    val reflectionId: UUID?,
    @Column(name = "submitted_at")
    val submittedAt: Instant?,
    @Column(name = "content_updated_at", nullable = false)
    val contentUpdatedAt: Instant,
)
