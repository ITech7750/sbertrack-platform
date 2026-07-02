package ru.itech.sbertrack.platform.reflection.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.reflection.domain.model.Reflection
import ru.itech.sbertrack.platform.reflection.dto.response.ReflectionResponse

@Component
class ReflectionMapper {
    fun toResponse(reflection: Reflection): ReflectionResponse =
        ReflectionResponse(
            id = reflection.id,
            submissionId = reflection.submissionId,
            studentId = reflection.studentId,
            answers = reflection.answers,
            summary = reflection.summary,
            createdAt = reflection.createdAt,
        )
}
