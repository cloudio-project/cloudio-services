package ch.hevs.cloudio.cloud.model

data class Transaction(
        val attributes: MutableMap<String, Attribute> = mutableMapOf()
)
