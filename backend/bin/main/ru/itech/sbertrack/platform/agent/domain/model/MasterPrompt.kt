package ru.itech.sbertrack.platform.agent.domain.model

import java.time.Instant
import java.util.UUID

data class MasterPrompt(
    val id: UUID = UUID.randomUUID(),
    val agentId: UUID,
    val title: String,
    val promptText: String,
    val version: Int,
    val status: MasterPromptStatus,
    val createdBy: UUID,
    val updatedAt: Instant = Instant.now(),
) {
    init {
        require(title.isNotBlank()) { "Название мастер-промпта обязательно" }
        require(promptText.isNotBlank()) { "Текст мастер-промпта обязателен" }
    }

    fun update(title: String, promptText: String): MasterPrompt =
        copy(title = title, promptText = promptText, version = version + 1, updatedAt = Instant.now())

    fun activate(): MasterPrompt = copy(status = MasterPromptStatus.ACTIVE, updatedAt = Instant.now())

    fun archive(): MasterPrompt = copy(status = MasterPromptStatus.ARCHIVED, updatedAt = Instant.now())
}
