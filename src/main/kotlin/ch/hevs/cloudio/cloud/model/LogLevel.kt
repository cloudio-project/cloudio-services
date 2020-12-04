package ch.hevs.cloudio.cloud.model

/**
 * Log levels that can be configured for individual endpoints.
 * @see ActionIdentifier.LOG_LEVEL
 */
enum class LogLevel {
    OFF,
    FATAL,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE,
    ALL
}
