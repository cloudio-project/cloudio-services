package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.CloudioObject
import java.util.*

data class JacksonObject(
        val conforms: String? = null,
        val objects: Optional<Map<String, JacksonObject>> = Optional.empty(),
        val attributes: Optional<Map<String, JacksonAttribute>> = Optional.empty()
) {
    fun toObject(): CloudioObject {
        return CloudioObject(
                conforms = conforms,
                objects = objects.get().mapValues { it.value.toObject() }.toMutableMap(),
                attributes = attributes.get().mapValues { it.value.toAttribute() }.toMutableMap()
        )
    }
}
