package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.AttributeType
import ch.hevs.cloudio.cloud.serialization.SerializationException
import java.util.*

data class JacksonAttribute(
        var constraint: Optional<AttributeConstraint> = Optional.empty(),
        var type: Optional<AttributeType> = Optional.empty(),
        var timestamp: Optional<Double> = Optional.empty(),
        var value: Optional<Any> = Optional.empty()
) {
    fun toAttribute() = Attribute(
            constraint = constraint.get().also { if (it == AttributeConstraint.Invalid) throw SerializationException() },
            type = type.get().also { if (it == AttributeType.Invalid || !it.checkType(value.orElse(null))) throw SerializationException() },
            timestamp = if (constraint.get() == AttributeConstraint.Static) null else timestamp.orElse(null)?.also { if (it < 0) throw SerializationException() },
            value = value.orElse(null)
    )
}
