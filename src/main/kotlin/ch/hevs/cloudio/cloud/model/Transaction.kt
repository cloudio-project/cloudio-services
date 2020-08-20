package ch.hevs.cloudio.cloud.model

/**
 * @brief Represents a transaction send from an endpoint to the cloud using the verb @transaction.
 *
 * A transaction contains a map of updated attributes.
 */
data class Transaction(
        val attributes: MutableMap<String, Attribute> = mutableMapOf()
)
