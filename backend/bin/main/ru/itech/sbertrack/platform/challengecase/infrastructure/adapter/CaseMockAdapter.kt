package ru.itech.sbertrack.platform.challengecase.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.challengecase.domain.model.PracticalCase
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import java.util.UUID

@Component
class CaseMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : CaseDataPort {
    override fun list(): List<PracticalCase> =
        dataStore.cases.values.sortedBy { it.createdAt }

    override fun findById(id: UUID): PracticalCase? =
        dataStore.cases[id]

    override fun save(practicalCase: PracticalCase): PracticalCase {
        dataStore.cases[practicalCase.id] = practicalCase
        return practicalCase
    }
}
