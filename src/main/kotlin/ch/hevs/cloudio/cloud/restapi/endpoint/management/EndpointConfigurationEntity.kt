package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.model.LogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EndpointConfiguration", description = "Endpoint configuration.")
data class EndpointConfigurationEntity(
        @Schema(description = "Configuration properties.")
        val properties: MutableMap<String, String>,

        @Schema(description = "TLS client certificate to use for endpoint authentication.", readOnly = true)
        var clientCertificate: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "TLS client private key used to authenticate the endpoint.", readOnly = true, required = false)
        var privateKey: String,

        @Schema(description = "Log level threshold for endpoint's log output send to the cloud.")
        var logLevel: LogLevel
)
