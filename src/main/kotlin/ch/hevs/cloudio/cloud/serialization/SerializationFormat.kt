package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*

interface SerializationFormat {
    fun detect(data: ByteArray): Boolean
    fun identifier(): String
    fun deserializeEndpointDataModel(data: ByteArray): EndpointDataModel
    fun deserializeNode(data: ByteArray): Node
    fun serializeAttribute(attribute: Attribute): ByteArray
    fun deserializeAttribute(data: ByteArray): Attribute
    fun deserializeAttributeSetCommand(data: ByteArray): AttributeSetCommand
    fun deserializeAttributeSetStatus(data: ByteArray): AttributeSetStatus
    fun deserializeTransaction(data: ByteArray): Transaction
    fun deserializeDelayedMessages(data: ByteArray): DelayedMessages
    fun deserializeLogMessage(data: ByteArray): LogMessage
    fun serializeLogLevel(logLevel: LogLevel): ByteArray
    fun deserializeLogLevel(data: ByteArray): LogLevel
    fun serializeJobExecCommand(jobExecCommand: JobExecCommand): ByteArray
    fun deserializeJobExecOutput(data: ByteArray): JobExecOutput
}

fun Collection<SerializationFormat>.detect(data: ByteArray) = this.firstOrNull { it.detect(data) }

fun Collection<SerializationFormat>.fromIdentifiers(identifiers: Collection<String>) = this.firstOrNull {
    (if (identifiers.isEmpty()) listOf("JSON") else identifiers).contains(it.identifier())
}
