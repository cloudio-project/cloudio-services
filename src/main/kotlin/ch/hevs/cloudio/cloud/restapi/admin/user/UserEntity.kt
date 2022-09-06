package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "User")
data class UserEntity(
        @Schema(description = "Username", readOnly = true, example = "john.doe")
        var name: String = "",

        @Schema(description = "User's email address.", example = "john.doe@theinternet.org")
        var email: String,

        @Schema(description = "User's global authorities.", example = "[\"HTTP_ACCESS\", \"HTTP_ENDPOINT_CREATION\"]")
        var authorities: Set<Authority>,

        @Schema(description = "True if the user is banned (blocked), false otherwise.", example = "false")
        var banned: Boolean,

        @Schema(description = "User's group memberships.", example = "[\"Managers\"]")
        var groupMemberships: Set<String>,

        @Schema(description = "User metadata.", example = "{\"department\": \"engineering\"}")
        var metadata: Map<String, Any>
) {
        constructor(user: User): this(user.userName, user.emailAddress.toString(), user.authorities, user.banned, user.groupMemberships.map { it.toString() }.toSet(), user.metaData)
}