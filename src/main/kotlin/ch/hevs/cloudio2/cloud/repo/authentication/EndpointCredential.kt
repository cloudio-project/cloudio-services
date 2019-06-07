package ch.hevs.cloudio2.cloud.repo.authentication

import ch.hevs.cloudio2.cloud.model.Authority
import ch.hevs.cloudio2.cloud.model.Permission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class EndpointCredential (
    @Id
    var userName: String = "",
    var permissions: Map<String, Permission> = emptyMap(),
    var authorities: Set<Authority> = emptySet()

)