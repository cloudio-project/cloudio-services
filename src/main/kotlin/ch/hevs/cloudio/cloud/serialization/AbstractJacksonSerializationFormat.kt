package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper

abstract class AbstractJacksonSerializationFormat(private val mapper: ObjectMapper) : SerializationFormat {
    init {
        mapper.apply {
            configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
            configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)
            configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
            configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true)
        }
    }

    override fun deserializeEndpointDataModel(endpoint: EndpointDataModel, data: ByteArray) {
        try {
            mapper.readerForUpdating(endpoint).readValue<EndpointDataModel>(data)
        } catch (_: Exception) {
            throw SerializationException("Error deserializing endpoint data model.")
        }
        if (endpoint.version == null) {
            endpoint.version = "v0.1"
        }
        if (endpoint.version == "v0.1" && endpoint.supportedFormats.isEmpty()) {
            endpoint.supportedFormats = setOf("JSON")
        }
        if (endpoint.supportedFormats.isEmpty() ||
                (endpoint.version != "v0.1" && endpoint.version != "v0.2")) {
            throw SerializationException("Error deserializing endpoint data model.")
        }
    }

    override fun deserializeNode(node: Node, data: ByteArray) {
        try {
            mapper.readerForUpdating(node).readValue<Node>(data)
        } catch (_: Exception) {
            throw SerializationException("Error deserializing node.")
        }
        if (node.online != null) {
            throw SerializationException("Error deserializing node.")
        }
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