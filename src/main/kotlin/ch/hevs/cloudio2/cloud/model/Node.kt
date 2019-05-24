package ch.hevs.cloudio2.cloud.model

data class Node(
        val implements: Set<String> = mutableSetOf(),
        val objects: MutableMap<String,CloudioObject> = mutableMapOf()
)