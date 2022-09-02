package ch.hevs.cloudio.cloud.restapi.endpoint.management

import ch.hevs.cloudio.cloud.model.LogLevel
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "EndpointConfiguration", description = "Endpoint configuration.")
data class EndpointConfigurationEntity(
        @Schema(description = "Configuration properties.", example = "{\"ch.hevs.cloudio.endpoint.uuid\": \"041c0e1e-b6d4-4f47-92b4-9f63343dbd28\", \"ch.hevs.cloudio.endpoint.hostUri\": \"cloudio.hevs.ch\"}")
        val properties: MutableMap<String, String>,

        @Schema(description = "TLS client certificate to use for endpoint authentication.", readOnly = true, example = "-----BEGIN CERTIFICATE-----...")
        var clientCertificate: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "TLS client private key used to authenticate the endpoint.", required = false, readOnly = true, example = "-----BEGIN RSA PRIVATE KEY-----...")
        var privateKey: String?,

        @Schema(description = "Log level threshold for endpoint's log output send to the cloud.", example = "WARN")
        var logLevel: LogLevel
)
