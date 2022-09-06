package ch.hevs.cloudio.cloud.restapi.endpoint.group

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EndpointGroup")
data class EndpointGroupEntity (
        val name: String,
        val metaData: Map<String, Any>
)