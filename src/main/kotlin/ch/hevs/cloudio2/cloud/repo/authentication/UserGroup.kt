package ch.hevs.cloudio2.cloud.repo.authentication

import ch.hevs.cloudio2.cloud.model.Authority
import ch.hevs.cloudio2.cloud.model.PrioritizedPermission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class UserGroup (

        @Id
        var userGroupName: String = "",
        var usersList: Set<String> =emptySet(),
        var permissions: Map<String, PrioritizedPermission> = emptyMap()
)
