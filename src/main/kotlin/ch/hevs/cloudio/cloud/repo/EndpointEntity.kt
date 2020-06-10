package ch.hevs.cloudio.cloud.repo

import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.LogLevel
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "Endpoint")
data class EndpointEntity(
        @Id
        var endpointUuid: UUID = UUID(0, 0),
        var friendlyName: String = "",
        var blocked: Boolean = false,
        var online: Boolean = false,    // TODO: Online information is saved to InfluxDB too, this could probably be removed.
        var logLevel: LogLevel = LogLevel.ERROR,
        var endpoint: EndpointDataModel = EndpointDataModel()
)
