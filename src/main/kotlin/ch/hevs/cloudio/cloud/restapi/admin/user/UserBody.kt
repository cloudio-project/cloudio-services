package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User

data class UserBody(
        var userName: String,
        var permissions: Map<String, PrioritizedPermission>,
        var groupMemberships: Set<String>,
        var authorities: Set<Authority>,
        var banned: Boolean
) {
    fun updateUser(user: User) {
        user.permissions = permissions
        user.userGroups = groupMemberships
        user.authorities = authorities
        user.banned = banned
    }
}

fun User.toUserBody() = UserBody(
        userName = userName,
        permissions = permissions,
        groupMemberships = userGroups,
        authorities = authorities,
        banned = banned
)
