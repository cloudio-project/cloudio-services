package ch.hevs.cloudio.cloud.model

data class Endpoint(
        val version: String? = null,
        val supportedFormats: Set<String> = emptySet(),
        val nodes: MutableMap<String, Node> = mutableMapOf()
)
