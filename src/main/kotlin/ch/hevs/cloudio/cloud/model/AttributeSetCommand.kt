package ch.hevs.cloudio.cloud.model

/**
 * Send from the cloud to an endpoint in order to change the value of an attribute using the action @set.
 * @see ActionIdentifier.ATTRIBUTE_SET
 */
data class AttributeSetCommand(
        /**
         * Correlation ID to send with the request to the endpoint has received in the @set message
         */
        val correlationID: String = "",

        /**
         * Timestamp.
         * UNIX Epoch time in seconds, floating point type allows higher resolution than seconds.
         */
        var timestamp: Double = -1.0,

        /**
         * Attribute's new value to set.
         * The type has to match [Attribute.type]
         */
        var value: Any? = null
)