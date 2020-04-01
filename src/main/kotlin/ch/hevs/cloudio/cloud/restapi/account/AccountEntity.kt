package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User

data class AccountEntity(
        var name: String,
        var permissions: Map<String, PrioritizedPermission>,
        var groupMemberships: Set<String>,
        var authorities: Set<Authority>
) {
    constructor(user: User): this(
            name = user.userName,
            permissions = user.permissions,
            groupMemberships = user.userGroups,
            authorities = user.authorities
    )
}
