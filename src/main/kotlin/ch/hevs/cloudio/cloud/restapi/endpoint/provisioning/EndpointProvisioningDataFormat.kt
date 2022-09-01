package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Endpoint provisioning data format.")
enum class EndpointProvisioningDataFormat {
    JSON,
    JAR_ARCHIVE
}
