package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

@ApiModel("EndpointListEntry", description = "Endpoint overview.")
data class ListEndpointEntity(
        @ApiModelProperty("The unique identifier of the given endpoint.", readOnly = true)
        val uuid: UUID,

        @ApiModelProperty("A user defined user-friendly name.", readOnly = true, example = "My endpoint")
        val friendlyName: String,

        @ApiModelProperty("If true the endpoint is blocked (can not connect).", readOnly = true, example = "false")
        val blocked: Boolean,

        @ApiModelProperty("If true the endpoint is actually online.", readOnly = true, example = "true")
        val online: Boolean,

        @ApiModelProperty("The permission the actually authenticated user has for the endpoint.", readOnly = true, example = "OWN")
        val permission: EndpointPermission
)
