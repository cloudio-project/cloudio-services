package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

@ApiModel(description = "Summarizes the permission the currently authenticated user has on a given endpoint and it's data model.")
data class EndpointPermissionEntity(
        @ApiModelProperty("UUID of the endpoint.", readOnly = true)
        val endpoint: UUID,

        @ApiModelProperty("User's permission concerning the endpoint as a whole.", readOnly = true, example = "ACCESS")
        val permission: EndpointPermission,

        @ApiModelProperty("User's permission for certain model elements of the endpoint.", readOnly = true,
                example = "[\"adc1/frequency/value\": \"READ\"]"
        )
        val modelPermissions: Map<String, EndpointModelElementPermission>
)
