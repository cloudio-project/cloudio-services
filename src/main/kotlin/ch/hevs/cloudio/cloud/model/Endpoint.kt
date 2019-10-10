package ch.hevs.cloudio.cloud.model

data class Endpoint(
        val nodes: MutableMap<String,Node> = mutableMapOf()
)