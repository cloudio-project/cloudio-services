package ch.hevs.cloudio2.cloud.repo.authentication

import ch.hevs.cloudio2.cloud.model.Authority
import ch.hevs.cloudio2.cloud.model.PrioritizedPermission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User (

        @Id
    var userName: String = "",
        var passwordHash: String = "",
        var permissions: Map<String, PrioritizedPermission> = emptyMap(),
        var authorities: Set<Authority> = emptySet()
)
