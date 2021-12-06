package ch.hevs.cloudio.cloud.model

/**
 * Represents a node in an endpoint's data model.
 *
 * Nodes are the root elements of any endpoint's data model. Only nodes can be added and removed dynamically to/from an endpoint.
 *
 * @see EndpointDataModel
 */
data class Node(
        /**
         * If true, the node is actually present and working, if false the node was once reported to be present by the endpoint, but is actually not available.
         */
        var online: Boolean = false,

        /**
         * A list of interfaces the node implements.
         *
         * Interfaces can be used to mark a given node to have a set (not exclusive) of attributes. It is not checked or validated at all by cloud.iO , but it can be handy to search for common
         * data structures in the database.
         */
        var implements: Set<String> = mutableSetOf(),

        /**
         * All objects of the node.
         */
        val objects: MutableMap<String, CloudioObject> = mutableMapOf()
)
