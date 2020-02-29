package ch.hevs.cloudio.cloud.repo.authentication

import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "UserGroup")
data class UserGroup(
        @Id
        var userGroupName: String = "",
        var permissions: Map<String, PrioritizedPermission> = emptyMap()
)
