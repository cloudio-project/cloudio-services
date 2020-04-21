package ch.hevs.cloudio.cloud.security

enum class PermissionPriority(val value: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    HIGHEST(3);
}