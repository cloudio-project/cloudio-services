package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.Node
import java.util.*

data class JacksonNode(
        val implements: Optional<Set<String>> = Optional.empty(),
        val objects: Optional<Map<String, JacksonObject>> = Optional.empty()
) {
    fun toNode() = Node(
            online = false,
            implements = implements.get(),
            objects = objects.get().mapValues { it.value.toObject() }.toMutableMap()
    )
}
