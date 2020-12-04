package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogsService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.LogMessage
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("logs-db", "default")
class DBLogsService(
        private val endpointRepository: EndpointRepository,
        serializationFormats: Collection<SerializationFormat>
) : AbstractLogsService(serializationFormats) {
    override fun logLevelChanged(endpointUuid: String, logLevel: LogLevel) {
        endpointRepository.findById(UUID.fromString(endpointUuid)).ifPresent {
            it.configuration.logLevel = logLevel
            endpointRepository.save(it)
        }
    }

    override fun newLog(endpointUuid: String, logMessage: LogMessage) {
        //nothing to do in mongo
    }
}
