package ch.hevs.cloudio.cloud.model

/**
 * Message send from an endpoint to the cloud containing log output.
 * @see ActionIdentifier.LOG_OUTPUT
 */
data class LogMessage(
        /**
         * The log level of the message.
         */
        var level: LogLevel = LogLevel.OFF,

        /**
         * Timestamp when the log message was created.
         * UNIX Epoch time in seconds, floating point type allows higher resolution than seconds.
         */
        var timestamp: Double = -1.0,

        /**
         * The actual log message.
         */
        var message: String = "",

        /**
         * Logger name.
         */
        var loggerName: String = "",

        /**
         * Log source.
         */
        var logSource: String = ""
)
