package ch.hevs.cloudio.cloud.model

data class EndpointDataModel(
        var version: String? = null,
        var supportedFormats: Set<String> = emptySet(),
        val nodes: MutableMap<String, Node> = mutableMapOf()
)
