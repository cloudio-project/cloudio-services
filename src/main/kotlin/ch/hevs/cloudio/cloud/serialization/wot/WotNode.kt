package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonProperty

data class WotNode(
        @JsonProperty("@context")
        val context: String = "",
        val id: String,
        val title: String,
        val securityDefinitions: Map<String, SecurityDefinition>,
        val security: Set<String>,
        val properties: Map<String, WotObject>,
        val events: Map<String, Event>

)
