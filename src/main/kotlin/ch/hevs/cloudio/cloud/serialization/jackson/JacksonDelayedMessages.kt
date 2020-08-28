package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.DelayedMessage
import ch.hevs.cloudio.cloud.model.DelayedMessages
import ch.hevs.cloudio.cloud.serialization.SerializationException
import java.util.*

data class JacksonDelayedMessages(
        var timestamp: Optional<Double> = Optional.empty(),
        var messages: Optional<Set<JacksonDelayedMessage>> = Optional.empty()) {

    data class JacksonDelayedMessage(
            var topic: Optional<String> = Optional.empty(),
            var data: Optional<Any> = Optional.empty()
    ) {
        fun toDelayedMessage() = DelayedMessage(
                topic =  topic.get(),
                data = data.get()
        )
    }

    fun toDelayedMessages() = DelayedMessages(
        timestamp = timestamp.get().also { if (it < 0) throw SerializationException() },
        messages = messages.get().map { it.toDelayedMessage() }.toSet()
    )
}
