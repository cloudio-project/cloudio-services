package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PropertyAffordance(
        override val type: String,
        override val properties: Map<String, DataSchemaInterface>?,
        override val required : Set<String>?,
        override val forms: Set<Form>,
        override val enum: Set<String>?
): InteractionAffordanceInterface, ObjectSchemaInterface