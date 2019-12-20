package ch.hevs.cloudio.cloud.serialization.wot

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
interface DataSchemaInterface{
    val type: String
    val enum: Set<String>?
}