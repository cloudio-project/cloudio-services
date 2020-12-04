package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogMessage
import ch.hevs.cloudio.cloud.serialization.SerializationException
import java.util.*

data class JacksonLogMessage(
        var level: Optional<LogLevel> = Optional.empty(),
        var timestamp: Optional<Double> = Optional.empty(),
        var message: Optional<String> = Optional.empty(),
        var loggerName: Optional<String> = Optional.empty(),
        var logSource: Optional<String> = Optional.empty()
) {
    fun toLogMessage() = LogMessage(
            level = level.get(),
            timestamp = timestamp.get().also { if (it < 0) throw SerializationException() },
            message = message.get(),
            loggerName = loggerName.orElse(""),
            logSource = logSource.orElse("")
    )
}
