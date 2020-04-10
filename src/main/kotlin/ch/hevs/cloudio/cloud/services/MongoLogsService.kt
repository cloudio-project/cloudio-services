package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogsService
import ch.hevs.cloudio.cloud.model.CloudioLogMessage
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogParameter
import ch.hevs.cloudio.cloud.repo.MONOGOEndpointEntityRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("logs-mongo", "default")
class MongoLogsService(val endpointEntityRepository: MONOGOEndpointEntityRepository) : AbstractLogsService() {


    override fun logLevelChange(endpointUuid: String, logParameter: LogParameter) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointUuid))
        if (endpointEntity != null) {
            endpointEntity.logLevel = LogLevel.valueOf(logParameter.level)
            endpointEntityRepository.save(endpointEntity)
        }
    }

    override fun newLog(endpointUuid: String, cloudioLogMessage: CloudioLogMessage) {
        //nothing to do in mongo
    }
}
