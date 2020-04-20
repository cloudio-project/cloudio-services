package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
class JSONSerializationFormat : SerializationFormat {
    private val mapper by lazy { Jackson2ObjectMapperBuilder.json().build<ObjectMapper>() }

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

    override fun deserializeTransaction(transaction: Transaction, data: ByteArray) {
        return mapper.readerForUpdating(transaction).readValue(data)
    }

    override fun deserializeDelayed(delayedContainer: DelayedContainer, data: ByteArray){
        return mapper.readerForUpdating(delayedContainer).readValue(data)
    }

    override fun deserializeCloudioLog(cloudioLogMessage: CloudioLogMessage, data: ByteArray) {
        return mapper.readerForUpdating(cloudioLogMessage).readValue(data)
    }

    override fun serializeLogParameter(logParameter: LogParameter): ByteArray {
        return mapper.writeValueAsBytes(logParameter)
    }

    override fun deserializeLogParameter(logParameter: LogParameter, data: ByteArray) {
        return mapper.readerForUpdating(logParameter).readValue(data)
    }

    override fun serializeJobParameter(jobParameter: JobParameter): ByteArray {
        return mapper.writeValueAsBytes(jobParameter)
    }

    override fun deserializeJobsLineOutput(jobsLineOutput: JobsLineOutput, data: ByteArray) {
        return mapper.readerForUpdating(jobsLineOutput).readValue(data)
    }
}