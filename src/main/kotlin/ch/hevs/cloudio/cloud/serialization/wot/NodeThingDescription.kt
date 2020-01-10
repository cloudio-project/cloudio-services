package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonProperty

data class NodeThingDescription(
        @JsonProperty("@context")
        val context: String = "",
        val id: String,
        val title: String,
        val securityDefinitions: Map<String, SecurityDefinition>,
        val security: Set<String>,
        val properties: Map<String, PropertyAffordance>,
        val events: Map<String, EventAffordance>

)
