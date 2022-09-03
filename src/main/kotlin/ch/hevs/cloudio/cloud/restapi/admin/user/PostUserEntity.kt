package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserCreationOptions")
data class PostUserEntity(
        @Schema(description = "Username.", writeOnly = true, example = "john.doe")
        val name: String,

        @Schema(description = "User's email address.", writeOnly = true, example = "john.doe@theinternet.org")
        val email: String,

        @Schema(description = "User's password.", writeOnly = true, example = "123456")
        val password: String,

        @Schema(description = "User's global authorities", writeOnly = true, required = false, defaultValue = "[\"HTTP_ACCESS\"]", example = "[\"HTTP_ACCESS\", \"HTTP_ENDPOINT_CREATION\"]")
        val authorities: Set<Authority>? = null,

        @Schema(description = "True to deactivate user, false to allow access", writeOnly = true, required = false, defaultValue = "false", example = "false")
        val banned: Boolean? = null,

        @Schema(description = "Groups to add the user to (Note that these groups have to exist!).", writeOnly = true,  required = false, defaultValue = "[]", example = "[]")
        val groupMemberships: Set<String>? = null,

        @Schema(description = "Metadata to set for the new created user.", writeOnly = true, required = false, defaultValue = "{}", example = "{}")
        val metaData: Map<String, Any>? = null
)