package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.EndpointDataModel
import java.util.*

data class JacksonEndpointDataModel(
        var version: Optional<String> = Optional.empty(),
        var messageFormatVersion: Optional<Int> = Optional.empty(),
        var supportedFormats: Optional<Set<String>> = Optional.empty(),
        val nodes: Optional<Map<String, JacksonNode>> = Optional.empty()) {
    fun toEndpointDataModel() = EndpointDataModel(
            version = version.orElse("unknown"),
            messageFormatVersion = messageFormatVersion.orElse(1),
            supportedFormats = if (messageFormatVersion.orElse(1) == 1) supportedFormats.orElse(setOf("JSON")) else supportedFormats.get(),
            nodes = nodes.get().mapValues { it.value.toNode() }.toMutableMap()
    )
}
