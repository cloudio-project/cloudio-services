package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority

data class ListUserEntity(
        val name: String,
        val email: String,
        val authorities: Set<Authority>,
        val banned: Boolean
)
