package ch.hevs.cloudio.cloud.model

enum class Permission (val value: Int) {
    DENY(0),
    READ(1),
    WRITE(2),
    CONFIGURE(3),
    GRANT(4),
    OWN(5);
}