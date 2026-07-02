package ru.itech.sbertrack.platform.track.domain.port

import ru.itech.sbertrack.platform.track.domain.model.Track
import java.util.UUID

interface TrackDataPort {
    fun list(): List<Track>
    fun findById(id: UUID): Track?
    fun save(track: Track): Track
}
