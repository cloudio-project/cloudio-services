package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "User")
data class UserEntity(
        var name: String,
        var email: String,
        var authorities: Set<Authority>,
        var banned: Boolean,
        var groupMemberships: Set<String>,
        var metadata: Map<String, Any>
)