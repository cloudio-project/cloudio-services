package ch.hevs.cloudio.cloud.restapi.endpoint.data

import ch.hevs.cloudio.cloud.model.Attribute

data class AttributeUpdateEvent(
    val id: String,
    val value: Attribute
)
