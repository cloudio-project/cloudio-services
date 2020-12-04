package ch.hevs.cloudio.cloud.restapi.endpoint.management

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

@ApiModel(description = "Endpoint details.")
data class EndpointEntity(
        @ApiModelProperty("The unique identifier of the endpoint.", readOnly = true)
        val uuid: UUID,

        @ApiModelProperty("User-friendly name of the endpoint.", example = "My endpoint")
        val friendlyName: String,

        @ApiModelProperty("If true the endpoint is banned (can not connect to the broker).", example = "false")
        val banned: Boolean,

        @ApiModelProperty("If true the endpoint is online.", readOnly = true, example = "true")
        val online: Boolean,

        @ApiModelProperty("Metadata assigned to the endpoint - Can be set to any particular JSON object/structure.")
        val metaData: Map<String, Any>,

        @ApiModelProperty("Software version actually running on the endpoint.", readOnly = true, example = "v0.2")
        val version: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @ApiModelProperty("Message format used by the endpoint", readOnly = true, example = "2", required = false)
        val messageFormatVersion: Int?,

        @ApiModelProperty("Serialization formats supported by the endpoint.", readOnly = true, example = "[\"CBOR\", \"JSON\"]")
        val supportedFormats: Set<String>
)
