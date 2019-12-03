package ch.hevs.cloudio.cloud.model

data class CloudioObject(
        val conforms: String? = null,
        val objects: MutableMap<String, CloudioObject> = mutableMapOf(),
        val attributes: MutableMap<String, Attribute> = mutableMapOf()
)