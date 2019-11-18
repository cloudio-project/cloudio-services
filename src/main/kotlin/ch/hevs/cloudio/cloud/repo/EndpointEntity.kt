package ch.hevs.cloudio.cloud.repo

import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.model.LogLevel
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Endpoint")
data class EndpointEntity(
        @Id
        var endpointUuid: String = "INVALID",
        var blocked: Boolean = false,
        var online: Boolean = false,
        var logLevel: LogLevel = LogLevel.ERROR,
        var endpoint: Endpoint = Endpoint()
)