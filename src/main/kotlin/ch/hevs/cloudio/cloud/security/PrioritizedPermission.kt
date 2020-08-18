package ch.hevs.cloudio.cloud.security

data class PrioritizedPermission(
        val permission: BrokerPermission,
        val priority: PermissionPriority
)
