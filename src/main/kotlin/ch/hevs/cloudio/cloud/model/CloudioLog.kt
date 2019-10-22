package ch.hevs.cloudio.cloud.model

data class CloudioLog(
        var level: LogLevel = LogLevel.OFF,
        var timestamp: Double = -1.0,
        var message: String ="empty",
        var loggerName: String = "empty",
        var logSource: String = "empty"
)