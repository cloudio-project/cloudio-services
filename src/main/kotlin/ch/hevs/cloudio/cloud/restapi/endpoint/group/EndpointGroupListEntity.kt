package ch.hevs.cloudio.cloud.restapi.endpoint.group

import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EndpointGroupListEntry", description = "Endpoint group overview used in endpoint group listing.")
data class EndpointGroupListEntity(
        @Schema(description = "The endpoint group name", readOnly = true, example = "BuildingAutomation")
        val name: String,

        @Schema(description = "The permission the currently authenticated user has for this endpoint group.", readOnly = true, example = "OWN")
        val permission: EndpointPermission
)
