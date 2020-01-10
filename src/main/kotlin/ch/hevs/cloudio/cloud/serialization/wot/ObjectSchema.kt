package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ObjectSchema(
        override val properties: Map<String, DataSchemaInterface>?,
        override val required: Set<String>?,
        override val type: String,
        override val enum: Set<String>?) : ObjectSchemaInterface {
}