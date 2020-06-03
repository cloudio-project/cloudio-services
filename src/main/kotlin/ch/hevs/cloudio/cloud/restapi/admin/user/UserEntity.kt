package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority

data class UserEntity(
        var name: String,
        var email: String,
        var authorities: Set<Authority>,
        var banned: Boolean,
        var groupMemberships: Set<String>,
        var metadata: Map<String, Any>
)