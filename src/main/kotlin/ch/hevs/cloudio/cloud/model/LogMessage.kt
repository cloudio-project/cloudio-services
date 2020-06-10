package ch.hevs.cloudio.cloud.model

data class LogMessage(
        var level: LogLevel = LogLevel.OFF,
        var timestamp: Double = -1.0,
        var message: String = "",
        var loggerName: String = "",
        var logSource: String = ""
)
