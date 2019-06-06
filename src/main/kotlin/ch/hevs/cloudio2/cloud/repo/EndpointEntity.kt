package ch.hevs.cloudio2.cloud.repo

import ch.hevs.cloudio2.cloud.model.Endpoint
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "Endpoint")
data class EndpointEntity(
        @Id
        var id: String = "INVALID",
        var blocked: Boolean = false,
        var online: Boolean = false,
        var endpoint: Endpoint = Endpoint()
)