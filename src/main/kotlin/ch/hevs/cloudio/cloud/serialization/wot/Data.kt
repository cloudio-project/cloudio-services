package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonProperty

data class Data(
        @JsonProperty("@type")
        val type: String
)