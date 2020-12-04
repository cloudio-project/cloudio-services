package ch.hevs.cloudio.cloud.model

/**
 * Status message returned by an endpoint with the action @didSet as a response to a @set message.
 * @see ActionIdentifier.ATTRIBUTE_DID_SET
 */
data class AttributeSetStatus(
        /**
         * Copy of the correlation ID the endpoint has received in the @set message.
         */
        val correlationID: String = "",

        /**
         * Timestamp.
         * UNIX Epoch time in seconds, floating point type allows higher resolution than seconds.
         */
        val timestamp: Double = -1.0,

        /**
         * Attribute's value.
         * The type has to match [Attribute.type]
         */
        val value: Any? = null
)
