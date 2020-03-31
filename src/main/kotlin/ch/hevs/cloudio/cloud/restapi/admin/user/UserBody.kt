package ch.hevs.cloudio.cloud.restapi.admin.user

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User

data class UserBody(
        var name: String,
        var permissions: Map<String, PrioritizedPermission>,
        var groupMemberships: Set<String>,
        var authorities: Set<Authority>,
        var banned: Boolean
) {
    constructor(user: User): this(
            name = user.userName,
            permissions = user.permissions,
            groupMemberships = user.userGroups,
            authorities = user.authorities,
            banned = user.banned
    )

    fun updateUser(user: User) {
        user.permissions = permissions.toMutableMap()
        user.userGroups = groupMemberships
        user.authorities = authorities
        user.banned = banned
    }
}
