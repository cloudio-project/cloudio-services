package ch.hevs.cloudio.cloud.repo.authentication

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "EndpointParameters")
data class EndpointParameters (
    @Id
    var endpointUuid: String ="",
    var friendlyName: String = ""
)
