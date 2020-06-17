package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("EndpointProvisioningOptions", description = "Options for endpoint provisioning.")
data class EndpointProvisioningOptionsEntity(
        @ApiModelProperty("Endpoint custom properties.", required = false)
        val customProperties: Map<String, String> = emptyMap()
)
