package ch.hevs.cloudio.cloud.restapi.endpoint.group

import ch.hevs.cloudio.cloud.restapi.endpoint.management.EndpointListEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EndpointGroup")
data class EndpointGroupEntity (
        @Schema(description = "The endpoint group name")
        val name: String,

        @Schema(description = "The endpoint group additional data")
        val metaData: Map<String, Any>,

        @Schema(description = "Endpoints belonging to the group")
        val endpoints: List<EndpointListEntity>
)