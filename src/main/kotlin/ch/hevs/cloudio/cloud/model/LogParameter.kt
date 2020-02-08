package ch.hevs.cloudio.cloud.model

data class LogParameter(
        val level: String = LogLevel.ERROR.toString()   // TODO: Use LogLevel in place of String.
)
