package ch.hevs.cloudio.cloud.repo.authentication

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "User")
data class User(
        @Id
        var userName: String = "",
        var passwordHash: String = "",
        var permissions: Map<String, PrioritizedPermission> = emptyMap(),
        var userGroups: Set<String> = emptySet(),
        var authorities: Set<Authority> = setOf(Authority.BROKER_ACCESS, Authority.HTTP_ACCESS),
        var banned: Boolean = false
)
