package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.util.*

@ApiModel("EndpointProvisioningConfiguration", description = "Endpoint configuration used when provisioning new endpoints.")
data class EndpointProvisioningConfigurationEntity(
        @ApiModelProperty("Endpoint UUID.")
        val endpoint: UUID,

        @ApiModelProperty("Endpoint configuration properties.", readOnly = true)
        val properties: Map<String, String>,

        @ApiModelProperty("TLS CA certificate to use for broker authentication.", readOnly = true)
        val caCertificate: String,

        @ApiModelProperty("TLS client certificate to use for endpoint authentication.", readOnly = true)
        val clientCertificate: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @ApiModelProperty("TLS client private key used to authenticate the endpoint.", readOnly = true, required = false)
        val clientPrivateKey: String?
)
