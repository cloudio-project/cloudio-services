package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.serialization.SerializationException
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
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

    override fun deserializeEndpointDataModel(data: ByteArray): EndpointDataModel = try {
        mapper.readerForUpdating(JacksonEndpointDataModel()).readValue<JacksonEndpointDataModel>(data).toEndpointDataModel()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing endpoint data model.")
    }

    override fun deserializeNode(data: ByteArray): Node = try {
        mapper.readerForUpdating(JacksonNode()).readValue<JacksonNode>(data).toNode()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing node.")
    }

    override fun serializeAttribute(attribute: Attribute): ByteArray = try {
        mapper.writeValueAsBytes(attribute)
    } catch (_: Exception) {
        throw SerializationException("Error serializing attribute.")
    }

    override fun deserializeAttribute(data: ByteArray) = try {
        mapper.readerForUpdating(JacksonAttribute()).readValue<JacksonAttribute>(data).toAttribute()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing attribute.")
    }

    override fun deserializeTransaction(data: ByteArray) = try {
        mapper.readerForUpdating(JacksonTransaction()).readValue<JacksonTransaction>(data).toTransaction()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing transaction.")
    }

    override fun deserializeDelayedMessages(data: ByteArray) = try {
        mapper.readerForUpdating(JacksonDelayedMessages()).readValue<JacksonDelayedMessages>(data).toDelayedMessages()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing delayed messages.")
    }

    override fun deserializeLogMessage(data: ByteArray) = try {
        mapper.readerForUpdating(JacksonLogMessage()).readValue<JacksonLogMessage>(data).toLogMessage()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing log message.")
    }

    override fun serializeLogLevel(logLevel: LogLevel): ByteArray = try {
        mapper.writeValueAsBytes(mapOf("level" to logLevel))
    } catch (_: Exception) {
        throw SerializationException("Error serializing log level.")
    }


    override fun deserializeLogLevel(data: ByteArray) = try {
        LogLevel.valueOf(mapper.readValue(data, Map::class.java)["level"] as String)
    } catch (_: Exception) {
        throw SerializationException("Error deserializing log message.")
    }

    override fun serializeJobExecCommand(jobExecCommand: JobExecCommand): ByteArray = try {
        mapper.writeValueAsBytes(jobExecCommand)
    } catch (_: Exception) {
        throw SerializationException("Error serializing job exec command.")
    }

    override fun deserializeJobExecOutput(data: ByteArray) = try {
        mapper.readerForUpdating(JacksonJobExecOutput()).readValue<JacksonJobExecOutput>(data).toJobExecOutput()
    } catch (_: Exception) {
        throw SerializationException("Error deserializing job exec output.")
    }
}