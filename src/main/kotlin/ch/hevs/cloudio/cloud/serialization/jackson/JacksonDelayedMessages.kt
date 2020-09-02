package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.DelayedMessage
import ch.hevs.cloudio.cloud.model.DelayedMessages
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.serialization.SerializationException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import java.util.*

data class JacksonDelayedMessages(
        var timestamp: Optional<Double> = Optional.empty(),
        var messages: Optional<List<JacksonDelayedMessage>> = Optional.empty()) {

    data class JacksonDelayedMessage(
            var topic: Optional<String> = Optional.empty(),
            var data: Optional<JsonNode> = Optional.empty()
    ) {
        fun toDelayedMessage(mapper: ObjectMapper) = DelayedMessage(
                topic =  topic.get(),
                data = data.get().let {
                    val id = ModelIdentifier(topic.get())
                    when (id.action) {
                        ActionIdentifier.ATTRIBUTE_UPDATE, ActionIdentifier.ATTRIBUTE_DID_SET -> mapper.treeToValue<JacksonAttribute>(it)!!.toAttribute()
                        ActionIdentifier.TRANSACTION -> mapper.treeToValue<JacksonTransaction>(it)!!.toTransaction()
                        ActionIdentifier.LOG_OUTPUT -> mapper.treeToValue<JacksonLogMessage>(it)!!.toLogMessage()
                        else -> throw SerializationException()
                    }
                }
        )
    }

    fun toDelayedMessages(mapper: ObjectMapper) = DelayedMessages(
        timestamp = timestamp.get().also { if (it < 0) throw SerializationException() },
        messages = messages.get().map { it.toDelayedMessage(mapper) }
    )
}
