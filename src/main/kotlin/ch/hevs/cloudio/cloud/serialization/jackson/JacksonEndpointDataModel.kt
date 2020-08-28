package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.serialization.SerializationException
import java.util.*

data class JacksonEndpointDataModel(
        var version: Optional<String> = Optional.empty(),
        var supportedFormats: Optional<Set<String>> = Optional.empty(),
        val nodes: Optional<Map<String, JacksonNode>> = Optional.empty()) {
    fun toEndpointDataModel() = EndpointDataModel(
            version = version.orElse("v0.1").also { if (it != "v0.1" && it != "v0.2") throw SerializationException() },
            supportedFormats = if (version.orElse("v0.1") == "v0.1") supportedFormats.orElse(setOf("JSON")) else supportedFormats.get(),
            nodes = nodes.get().mapValues { it.value.toNode() }.toMutableMap()
    )
}
