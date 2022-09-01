package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EndpointProvisioningOptions", description = "Options for endpoint provisioning.")
data class EndpointProvisioningOptionsEntity(
        @Schema(description = "Endpoint custom properties.", required = false)
        val customProperties: Map<String, String> = emptyMap()
)
