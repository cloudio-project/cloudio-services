package ch.hevs.cloudio.cloud.model

data class PrioritizedPermission(
        val permission: Permission,
        val priority: PermissionPriority
)
