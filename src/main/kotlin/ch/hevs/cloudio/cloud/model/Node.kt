package ch.hevs.cloudio.cloud.model

data class Node(
        val implements: Set<String> = mutableSetOf(),
        val objects: MutableMap<String,CloudioObject> = mutableMapOf()
)