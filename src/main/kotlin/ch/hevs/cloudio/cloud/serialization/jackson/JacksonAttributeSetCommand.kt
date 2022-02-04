package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.AttributeSetCommand
import ch.hevs.cloudio.cloud.model.AttributeSetStatus
import ch.hevs.cloudio.cloud.serialization.SerializationException
import java.util.*

data class JacksonAttributeSetCommandOrStatus(
    var correlationID: Optional<String> = Optional.empty(),
    var timestamp: Optional<Double> = Optional.empty(),
    var value: Optional<Any> = Optional.empty()
) {
    fun toAttributeSetCommand() = AttributeSetCommand(
        correlationID = correlationID.orElse(""),
        timestamp = timestamp.orElseThrow{ SerializationException() }.also { if (it < 0) throw SerializationException() },
        value = value.orElseThrow{ SerializationException() }
    )
    fun toAttributeSetStatus() = AttributeSetStatus(
        correlationID = correlationID.orElse(""),
        timestamp = timestamp.orElseThrow{ SerializationException() }.also { if (it < 0) throw SerializationException() },
        value = value.orElseThrow{ SerializationException() }
    )
}
