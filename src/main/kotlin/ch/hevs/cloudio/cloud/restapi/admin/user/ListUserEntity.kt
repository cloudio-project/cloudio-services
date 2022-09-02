package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserListEntry", example =
"{\"name\": \"john.doe\", \"email\": \"johndoe@theinternet.org\", \"authorities\": [\"BROKER_ACCESS\", \"HTTP_ACCESS\"], \"banned\": false }")
data class ListUserEntity(
        val name: String,
        val email: String,
        val authorities: Set<Authority>,
        val banned: Boolean
)
