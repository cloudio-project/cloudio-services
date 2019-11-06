package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WotObject(
        @JsonProperty("@type")
        val type: String,
        val properties: Map<String, WotObject>?,
        val forms: Set<Form>?
)