package ch.hevs.cloudio.cloud.model

data class Endpoint(
        val version: String = "",
        val supportedFormat: Set<String> = mutableSetOf(),
        val nodes: MutableMap<String, Node> = mutableMapOf()
)
