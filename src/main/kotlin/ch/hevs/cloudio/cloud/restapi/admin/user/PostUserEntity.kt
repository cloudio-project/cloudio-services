package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserCreationOptions", example = "{\"name\": \"john.doe\", \"email\": \"johndoe@theinternet.org\", \"password\": \"123456\", \"authorities\": [\"BROKER_ACCESS\", \"HTTP_ACCESS\"]}")
data class PostUserEntity(
        val name: String,
        val email: String,
        val password: String,
        val authorities: Set<Authority> = Authority.DEFAULT_AUTHORITIES,
        val banned: Boolean = false,
        val groupMemberships: Set<String> = emptySet(),
        val metaData: Map<String, Any> = emptyMap()
)