package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.model.LogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Endpoint configuration.")
data class EndpointConfigurationEntity(
        @ApiModelProperty("Configuration properties.")
        val properties: MutableMap<String, String>,

        @ApiModelProperty("TLS client certificate to use for endpoint authentication.", readOnly = true)
        var clientCertificate: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @ApiModelProperty("TLS client private key used to authenticate the endpoint.", readOnly = true, required = false)
        var privateKey: String,

        @ApiModelProperty("Log level threshold for endpoint's log output send to the cloud.")
        var logLevel: LogLevel
)
