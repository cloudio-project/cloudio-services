package ch.hevs.cloudio.cloud.model

/**
 * Groups recursively a set of attributes into entities.
 * @see Node
 */
data class CloudioObject(
        /**
         * An object can conform to a given scheme. A scheme defines the exact set of attributes an object has to contain.
         * Note that cloud.iO does not enforce or validates this conformity at all, however it can serve to mark compatible data structures.
         */
        var conforms: String? = null,

        /**
         * Child objects.
         */
        val objects: MutableMap<String, CloudioObject> = mutableMapOf(),

        /**
         * Object's attributes.
         */
        val attributes: MutableMap<String, Attribute> = mutableMapOf()
)
