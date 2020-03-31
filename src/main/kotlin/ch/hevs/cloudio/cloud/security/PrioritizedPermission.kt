package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority

data class PrioritizedPermission(
        val permission: Permission,
        val priority: PermissionPriority
)
