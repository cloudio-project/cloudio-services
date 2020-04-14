package ch.hevs.cloudio.cloud.model

data class Endpoint(
        val version: String = "v0.1",
        val supportedFormats: Set<String> = mutableSetOf(),
        val nodes: MutableMap<String, Node> = mutableMapOf()
)
