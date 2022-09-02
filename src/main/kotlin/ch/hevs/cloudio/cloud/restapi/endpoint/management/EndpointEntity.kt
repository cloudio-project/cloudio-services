package ch.hevs.cloudio.cloud.restapi.endpoint.management

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(name = "Endpoint", description = "Endpoint details.")
data class EndpointEntity(
        @Schema(description = "The unique identifier of the endpoint.", readOnly = true)
        val uuid: UUID,

        @Schema(description = "User-friendly name of the endpoint.", example = "My endpoint")
        val friendlyName: String,

        @Schema(description = "If true the endpoint is banned (can not connect to the broker).", example = "false")
        val banned: Boolean,

        @Schema(description = "If true the endpoint is online.", readOnly = true, example = "true")
        val online: Boolean,

        @Schema(description = "Metadata assigned to the endpoint - Can be set to any particular JSON object/structure.")
        val metaData: Map<String, Any>,

        @Schema(description = "Software version actually running on the endpoint.", readOnly = true, example = "v0.2")
        val version: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "Message format used by the endpoint", readOnly = true, example = "2", required = false)
        val messageFormatVersion: Int?,

        @Schema(description = "Serialization formats supported by the endpoint.", readOnly = true, example = "[\"CBOR\", \"JSON\"]")
        val supportedFormats: Set<String>,

        @Schema(description = "List of the endpoint group memberships.")
        var groupMemberships: Set<String>
)
