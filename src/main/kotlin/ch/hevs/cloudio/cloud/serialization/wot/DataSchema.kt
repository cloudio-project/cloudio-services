package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DataSchema (
        override val type: String,
        override val enum: Set<String>?
): DataSchemaInterface