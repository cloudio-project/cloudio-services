package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.security.EndpointPermission
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(description = "Endpoint overview used in endpoint listing.")
data class EndpointListEntity(
        @Schema(description = "The unique identifier of the endpoint.", readOnly = true)
        val uuid: UUID,

        @Schema(description = "User-friendly name of the endpoint.", readOnly = true, example = "My endpoint")
        val friendlyName: String,

        @Schema(description = "If true the endpoint is banned (can not connect to the broker).", readOnly = true, example = "false")
        val banned: Boolean,

        @Schema(description = "If true the endpoint is online.", readOnly = true, example = "true")
        val online: Boolean,

        @Schema(description = "The permission the currently authenticated user has for this endpoint.", readOnly = true, example = "OWN")
        val permission: EndpointPermission
)
