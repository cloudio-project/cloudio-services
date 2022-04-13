package ch.hevs.cloudio.cloud.restapi.endpoint.group

data class EndpointGroupEntity (
        val name: String,
        val metaData: Map<String, Any>
)