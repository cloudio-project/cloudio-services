package ch.hevs.cloudio.cloud.model

data class CloudioLogMessage(
        var level: LogLevel = LogLevel.OFF,
        var timestamp: Double = -1.0,
        var message: String = "",
        var loggerName: String = "Unnamed logger",
        var logSource: String = "Unknown Source"
)
