package ch.hevs.cloudio.cloud.restapi.endpoint.management

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

@ApiModel("Endpoint", description = "Endpoint details.")
data class EndpointEntity(
        @ApiModelProperty("The unique identifier of the given endpoint.", readOnly = true)
        val uuid: UUID,

        @ApiModelProperty("A user defined user-friendly name.", example = "My endpoint")
        val friendlyName: String,

        @ApiModelProperty("If true the endpoint is blocked (can not connect).", example = "false")
        val blocked: Boolean,

        @ApiModelProperty("If true the endpoint is actually online.", readOnly = true, example = "true")
        val online: Boolean,

        @ApiModelProperty("Metadata assigned to the endpoint.")
        val metaData: Map<String, Any>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @ApiModelProperty("cloud.iO endpoint version actually running.", readOnly = true, example = "v0.2", required = false)
        val version: String?,

        @ApiModelProperty("Supported serialization formats of the endpoint.", readOnly = true, example = "[\"JSON\", \"CBOR\"]")
        val supportedFormats: Set<String>
)
