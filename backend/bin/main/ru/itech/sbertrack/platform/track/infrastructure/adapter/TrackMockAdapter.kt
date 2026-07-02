package ru.itech.sbertrack.platform.track.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.track.domain.model.Track
import ru.itech.sbertrack.platform.track.domain.port.TrackDataPort
import java.util.UUID

@Component
class TrackMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : TrackDataPort {
    override fun list(): List<Track> =
        dataStore.tracks.values.sortedBy { it.createdAt }

    override fun findById(id: UUID): Track? =
        dataStore.tracks[id]

    override fun save(track: Track): Track {
        dataStore.tracks[track.id] = track
        return track
    }
}
