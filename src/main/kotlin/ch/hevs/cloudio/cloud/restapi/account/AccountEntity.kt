package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.dao.User
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.util.*

data class AccountEntity(
        var name: String,
        var email: String,
        var authorities: Set<Authority>,
        var groupMemberships: List<String>,
        var metadata: Map<String, Any>
) {
    constructor(user: User) : this(
            name = user.userName,
            email = user.emailAddress.toString(),
            authorities = user.authorities,
            groupMemberships = user.groupMemberships.map { it.groupName },
            metadata = user.metaData
    )
}
