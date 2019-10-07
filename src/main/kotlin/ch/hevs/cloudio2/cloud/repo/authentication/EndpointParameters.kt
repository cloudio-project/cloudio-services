package ch.hevs.cloudio2.cloud.repo.authentication

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class EndpointParameters (
    @Id
    var UUID: String ="",
    var friendlyName: String = ""

)