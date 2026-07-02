package ru.itech.sbertrack.platform.challengecase.application.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore

class CaseMapperTest {
    @Test
    fun `case mapper exposes domain fields in response`() {
        val practicalCase = MockPlatformDataStore().cases.values.first()
        val response = CaseMapper().toResponse(practicalCase)

        assertEquals(practicalCase.id, response.id)
        assertEquals(practicalCase.title, response.title)
        assertEquals(practicalCase.competencyWeights, response.competencyWeights)
    }
}
