package ch.hevs.cloudio.cloud.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Represents an attribute (data model leaf) of a cloud.iO endpoint's data model. Send by an endpoint on an attribute update (@update) and as port of a @online or @nodeAdded message.
 * @see ActionIdentifier.ATTRIBUTE_UPDATE
 * @see ActionIdentifier.ENDPOINT_ONLINE
 * @see ActionIdentifier.NODE_ADDED
 */
data class Attribute(
        /**
         * Defines if the attribute can be read or written from the cloud or if it is static.
         */
        var constraint: AttributeConstraint = AttributeConstraint.Invalid,

        /**
         * Data type of the attribute.
         */
        var type: AttributeType = AttributeType.Invalid,

        /**
         * Timestamp of the last value change (reported to the cloud) of the attribute or timestamp of the set request from the the cloud to the endpoint.
         * UNIX Epoch time in seconds, floating point type allows higher resolution than seconds.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var timestamp: Double? = null,

        /**
         * Attribute's value.
         * The type has to match [Attribute.type]
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        var value: Any? = null
)
