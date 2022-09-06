package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(name = "EndpointProvisioningConfiguration", description = "Endpoint configuration used when provisioning new endpoints.")
data class EndpointProvisioningConfigurationEntity(
        @Schema(description = "Endpoint UUID.", readOnly = true, example = "15830bd8-5c98-4eeb-80f2-7607d3280973")
        val endpoint: UUID,

        @Schema(description = "Endpoint configuration properties.", readOnly = true, example = "{}")
        val properties: Map<String, String>,

        @Schema(description = "TLS CA certificate to use for broker authentication.", readOnly = true, example = "-----BEGIN CERTIFICATE-----\n...")
        val caCertificate: String,

        @Schema(description = "TLS client certificate to use for endpoint authentication.", readOnly = true, example = "-----BEGIN CERTIFICATE-----\n...")
        val clientCertificate: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "TLS client private key used to authenticate the endpoint.", readOnly = true, required = false, example = "-----BEGIN RSA PRIVATE KEY-----\n...")
        val clientPrivateKey: String?
)
