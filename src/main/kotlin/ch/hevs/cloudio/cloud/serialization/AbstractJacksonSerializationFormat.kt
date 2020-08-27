package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

abstract class AbstractJacksonSerializationFormat(private val mapper: ObjectMapper) : SerializationFormat {
    init {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    }

    override fun deserializeEndpoint(endpoint: EndpointDataModel, data: ByteArray) {
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
        try {
            mapper.readerForUpdating(attribute).readValue<Attribute>(data)
        } catch (_: Exception) {
            throw SerializationException("Error deserializing attribute.")
        }
        if (attribute.constraint == AttributeConstraint.Invalid ||
                attribute.type == AttributeType.Invalid ||
                attribute.timestamp < 0 ||
                !attribute.type.checkType(attribute.value)) {
            throw SerializationException("Error deserializing attribute.")
        }
    }

    override fun deserializeTransaction(transaction: Transaction, data: ByteArray) {
        return mapper.readerForUpdating(transaction).readValue(data)
    }

    override fun deserializeDelayed(delayedMessages: DelayedMessages, data: ByteArray) {
        return mapper.readerForUpdating(delayedMessages).readValue(data)
    }

    override fun deserializeCloudioLog(logMessage: LogMessage, data: ByteArray) {
        return mapper.readerForUpdating(logMessage).readValue(data)
    }

    override fun serializeLogLevel(logLevel: LogLevel): ByteArray {
        return mapper.writeValueAsBytes(mapOf("level" to logLevel))
    }

    override fun deserializeLogLevel(data: ByteArray): LogLevel {
        return LogLevel.valueOf(mapper.readValue(data, Map::class.java)["level"] as String)
    }

    override fun serializeJobParameter(jobExecCommand: JobExecCommand): ByteArray {
        return mapper.writeValueAsBytes(jobExecCommand)
    }

    override fun deserializeJobsLineOutput(jobExecOutput: JobExecOutput, data: ByteArray) {
        return mapper.readerForUpdating(jobExecOutput).readValue(data)
    }
}