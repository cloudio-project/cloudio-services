package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority

data class PostUserEntity(
        val name: String,
        val email: String,
        val password: String,
        val authorities: Set<Authority> = Authority.DEFAULT_AUTHORITIES,
        val banned: Boolean = false,
        val groupMemberships: Set<String> = emptySet(),
        val metaData: Map<String, Any> = emptyMap()
)