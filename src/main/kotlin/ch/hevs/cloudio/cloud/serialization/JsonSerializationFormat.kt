package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule


internal object JsonSerializationFormat {
    private val mapper: ObjectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    fun detect(data: ByteArray): Boolean = data.size > 0 && data[0] == '{'.toByte()

    fun deserializeEndpoint(endpoint: Endpoint, data: ByteArray) {
        return mapper.readerForUpdating(endpoint).readValue(data)
    }

    fun deserializeNode(node: Node, data: ByteArray) {
        return mapper.readerForUpdating(node).readValue(data)
    }

    fun deserializeObject(obj: CloudioObject, data: ByteArray) {
        return mapper.readerForUpdating(obj).readValue(data)
    }

    fun serializeAttribute(attribute: Attribute): ByteArray {
        return mapper.writeValueAsBytes(attribute)
    }

    fun deserializeAttribute(attribute: Attribute, data: ByteArray) {
        return mapper.readerForUpdating(attribute).readValue(data)
    }

    fun deserializeCloudioLog(cloudioLog: CloudioLog, data: ByteArray){
        return mapper.readerForUpdating(cloudioLog).readValue(data)
    }

    fun serializeLogParameter(logParameter: LogParameter): ByteArray {
        return mapper.writeValueAsBytes(logParameter)
    }

    fun deserializeLogParameter(logParameter: LogParameter, data: ByteArray){
        return mapper.readerForUpdating(logParameter).readValue(data)
    }
    fun serializeJobParameter(jobParameter: JobParameter): ByteArray {
        return mapper.writeValueAsBytes(jobParameter)
    }
}