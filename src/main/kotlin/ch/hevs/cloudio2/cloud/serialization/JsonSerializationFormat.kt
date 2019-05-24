package ch.hevs.cloudio2.cloud.serialization

import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.model.CloudioObject
import ch.hevs.cloudio2.cloud.model.Endpoint
import ch.hevs.cloudio2.cloud.model.Node
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule


internal object JsonSerializationFormat: SerializationFormat {
    private val mapper: ObjectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    override fun detect(data: ByteArray): Boolean = data.size > 0 && data[0] == '{'.toByte()

    override fun deserializeEndpoint(endpoint: Endpoint, data: ByteArray) {
        return mapper.readerForUpdating(endpoint).readValue(data)
    }

    override fun deserializeNode(node: Node, data: ByteArray) {
        return mapper.readerForUpdating(node).readValue(data)
    }

    override fun deserializeObject(obj: CloudioObject, data: ByteArray) {
        return mapper.readerForUpdating(obj).readValue(data)
    }

    override fun serializeAttribute(attribute: Attribute): ByteArray {
        return mapper.writeValueAsBytes(attribute)
    }

    override fun deserializeAttribute(attribute: Attribute, data: ByteArray) {
        return mapper.readerForUpdating(attribute).readValue(data)
    }
}