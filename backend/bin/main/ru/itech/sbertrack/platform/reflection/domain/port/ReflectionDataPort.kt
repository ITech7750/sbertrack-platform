package ru.itech.sbertrack.platform.reflection.domain.port

import ru.itech.sbertrack.platform.reflection.domain.model.Reflection
import java.util.UUID

interface ReflectionDataPort {
    fun findBySubmissionId(submissionId: UUID): Reflection?
    fun save(reflection: Reflection): Reflection
}
