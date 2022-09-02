package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(name = "EndpointPermission", description = "Summarizes the permission the currently authenticated user has on a given endpoint and it's data model.")
data class EndpointPermissionEntity(
        @Schema(description = "UUID of the endpoint.", readOnly = true)
        val endpoint: UUID,

        @Schema(description = "User's permission concerning the endpoint as a whole.", readOnly = true, example = "ACCESS")
        val permission: EndpointPermission,

        @Schema(description = "User's permission for certain model elements of the endpoint.", readOnly = true,
                example = "[\"adc1/frequency/value\": \"READ\"]"
        )
        val modelPermissions: Map<String, EndpointModelElementPermission>
)
