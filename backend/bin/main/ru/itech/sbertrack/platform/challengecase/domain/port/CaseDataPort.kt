package ru.itech.sbertrack.platform.challengecase.domain.port

import ru.itech.sbertrack.platform.challengecase.domain.model.PracticalCase
import java.util.UUID

interface CaseDataPort {
    fun list(): List<PracticalCase>
    fun findById(id: UUID): PracticalCase?
    fun save(practicalCase: PracticalCase): PracticalCase
}
