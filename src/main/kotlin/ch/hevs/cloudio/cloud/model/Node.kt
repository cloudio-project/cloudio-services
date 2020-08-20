package ch.hevs.cloudio.cloud.model

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @brief Represents a node in an endpoint's data model.
 *
 * Nodes are the root elements of any endpoint's data model. Only nodes can be added and removed dynamically to/from an endpoint.
 */
data class Node(
        var online: Boolean = false,
        val implements: Set<String> = mutableSetOf(),
        val objects: MutableMap<String, CloudioObject> = mutableMapOf()
)
