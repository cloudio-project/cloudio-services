package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
interface ObjectSchemaInterface: DataSchemaInterface {
    val properties: Map<String, DataSchemaInterface>?
    val required : Set<String>?
}