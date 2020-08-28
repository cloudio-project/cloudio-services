package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*

interface SerializationFormat {
    fun detect(data: ByteArray): Boolean
    fun identifier(): String
    fun deserializeEndpointDataModel(endpoint: EndpointDataModel, data: ByteArray)
    fun deserializeNode(node: Node, data: ByteArray)
    fun deserializeObject(obj: CloudioObject, data: ByteArray)
    fun serializeAttribute(attribute: Attribute): ByteArray
    fun deserializeAttribute(attribute: Attribute, data: ByteArray)
    fun deserializeTransaction(transaction: Transaction, data: ByteArray)
    fun deserializeDelayed(delayedMessages: DelayedMessages, data: ByteArray)
    fun deserializeCloudioLog(logMessage: LogMessage, data: ByteArray)
    fun serializeLogLevel(logLevel: LogLevel): ByteArray
    fun deserializeLogLevel(data: ByteArray): LogLevel
    fun serializeJobParameter(jobExecCommand: JobExecCommand): ByteArray
    fun deserializeJobsLineOutput(jobExecOutput: JobExecOutput, data: ByteArray)
}

fun Collection<SerializationFormat>.detect(data: ByteArray) = this.firstOrNull { it.detect(data) }

fun Collection<SerializationFormat>.fromIdentifiers(identifiers: Collection<String>) = this.firstOrNull {
    (if (identifiers.isEmpty()) listOf("JSON") else identifiers).contains(it.identifier())
}
