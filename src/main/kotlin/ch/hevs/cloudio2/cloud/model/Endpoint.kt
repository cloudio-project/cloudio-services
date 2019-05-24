package ch.hevs.cloudio2.cloud.model

data class Endpoint(
        val nodes: MutableMap<String,Node> = mutableMapOf()
)