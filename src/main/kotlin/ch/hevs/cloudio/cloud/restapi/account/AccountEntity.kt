package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.Authority

data class AccountEntity(
        var name: String,
        var email: String,
        var authorities: Set<Authority>,
        var groupMemberships: List<String>,
        var metadata: Map<String, Any>
)
