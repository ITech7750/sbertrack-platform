package ru.itech.sbertrack.platform.reflection.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.reflection.domain.model.Reflection
import ru.itech.sbertrack.platform.reflection.domain.port.ReflectionDataPort
import java.util.UUID

@Component
class ReflectionMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : ReflectionDataPort {
    override fun findBySubmissionId(submissionId: UUID): Reflection? =
        dataStore.reflections[submissionId]

    override fun save(reflection: Reflection): Reflection {
        dataStore.reflections[reflection.submissionId] = reflection
        return reflection
    }
}
