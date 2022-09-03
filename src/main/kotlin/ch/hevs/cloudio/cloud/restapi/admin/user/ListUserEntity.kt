package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserListEntry")
data class ListUserEntity(
        @Schema(description = "Username.", readOnly = true, example = "john.doe")
        val name: String,

        @Schema(description = "User's email address.", readOnly = true, example = "john.doe@theinternet.org")
        val email: String,

        @Schema(description = "User's global authorities", readOnly = true, example = "[\"HTTP_ACCESS\", \"HTTP_ENDPOINT_CREATION\"]")
        val authorities: Set<Authority>,

        @Schema(description = "True if the user is banned (blocked), false otherwise.", readOnly = true, example = "false")
        val banned: Boolean
) {
        constructor(user: User) : this(user.userName, user.emailAddress.toString(), user.authorities, user.banned)
}
