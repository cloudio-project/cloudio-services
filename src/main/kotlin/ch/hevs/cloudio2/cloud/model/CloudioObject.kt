package ch.hevs.cloudio2.cloud.model

data class CloudioObject(
        val conforms: String? = null,
        val objects: MutableMap<String,CloudioObject> = mutableMapOf(),
        val attributes: MutableMap<String,Attribute> = mutableMapOf()
)