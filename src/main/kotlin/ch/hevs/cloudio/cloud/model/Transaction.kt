package ch.hevs.cloudio.cloud.model

/**
 * Represents a transaction send from an endpoint to the cloud using the verb @transaction.
 *
 * A transaction contains a map of updated attributes.
 */
data class Transaction(
        /**
         * Map of all attributes that have been changed during the transaction.
         */
        val attributes: MutableMap<String, Attribute> = mutableMapOf()
)
