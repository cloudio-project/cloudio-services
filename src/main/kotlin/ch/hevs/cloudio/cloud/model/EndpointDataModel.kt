package ch.hevs.cloudio.cloud.model

/**
 * Represents the data model of an endpoint.
 * @see ActionIdentifier.ENDPOINT_ONLINE
 */
data class EndpointDataModel(
        /**
         * The endpoint protocol version.
         * Valid values are `v0.1` and `v0.2`.
         */
        var version: String = "unknown",

        /**
         * The message format used by the endpoint.
         */
        var messageFormatVersion: Int? = null,

        /**
         * List of serialization formats supported by the endpoint.
         * Currently supported formats are `JSON` and `CBOR`.
         * An endpoint has to support at least one format, if no format is declared the cloud assumes the default format `JSON`.
         */
        var supportedFormats: Set<String> = emptySet(),

        /**
         * List of all nodes of the endpoint.
         * In the @online message, this list contains only nodes connected whereas in the database all nodes that were ever reported are present.
         */
        val nodes: MutableMap<String, Node> = mutableMapOf()
)
