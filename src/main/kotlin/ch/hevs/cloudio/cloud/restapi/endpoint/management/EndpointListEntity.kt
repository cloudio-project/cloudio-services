package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

@ApiModel(description = "Endpoint overview used in endpoint listing.")
data class EndpointListEntity(
        @ApiModelProperty("The unique identifier of the endpoint.", readOnly = true)
        val uuid: UUID,

        @ApiModelProperty("User-friendly name of the endpoint.", readOnly = true, example = "My endpoint")
        val friendlyName: String,

        @ApiModelProperty("If true the endpoint is banned (can not connect to the broker).", readOnly = true, example = "false")
        val banned: Boolean,

        @ApiModelProperty("If true the endpoint is online.", readOnly = true, example = "true")
        val online: Boolean,

        @ApiModelProperty("The permission the currently authenticated user has for this endpoint.", readOnly = true, example = "OWN")
        val permission: EndpointPermission
)
