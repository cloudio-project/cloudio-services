package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.LogLevel
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("logs-db", "default")
class DBLogService(
        private val endpointRepository: EndpointRepository
) : AbstractLogService(ignoresLogMessages = true) {
    override fun logLevelChanged(endpointUUID: UUID, level: LogLevel) {
        endpointRepository.findById(endpointUUID).ifPresent {
            it.configuration.logLevel = level
            endpointRepository.save(it)
        }
    }
}
