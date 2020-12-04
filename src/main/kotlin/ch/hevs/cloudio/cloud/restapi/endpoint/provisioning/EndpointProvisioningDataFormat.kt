package ch.hevs.cloudio.cloud.restapi.endpoint.provisioning

import io.swagger.annotations.ApiModel

@ApiModel("Endpoint provisioning data format.")
enum class EndpointProvisioningDataFormat {
    JSON,
    JAR_ARCHIVE
}
