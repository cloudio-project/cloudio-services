package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User
import org.springframework.security.crypto.password.PasswordEncoder

data class PostUserBody(
        var password: String,
        var permissions: Map<String, PrioritizedPermission> = emptyMap(),
        var groupMemberships: Set<String> = emptySet(),
        var authorities: Set<Authority> = setOf(Authority.BROKER_ACCESS, Authority.HTTP_ACCESS),
        var banned: Boolean = false
) {
    fun toUser(userName: String, passwordEncoder: PasswordEncoder) = User(
            userName = userName,
            passwordHash = passwordEncoder.encode(password),
            permissions = permissions.toMutableMap(),
            userGroups = groupMemberships,
            authorities = authorities,
            banned = banned
    )
}
