package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*

interface SerializationFormat {
    fun detect(data: ByteArray): Boolean
    fun identifier(): String
    fun deserializeEndpoint(endpoint: Endpoint, data: ByteArray)
    fun deserializeNode(node: Node, data: ByteArray)
    fun deserializeObject(obj: CloudioObject, data: ByteArray)
    fun serializeAttribute(attribute: Attribute): ByteArray
    fun deserializeAttribute(attribute: Attribute, data: ByteArray)
    fun deserializeTransaction(transaction: Transaction, data: ByteArray)
    fun deserializeDelayed(delayedContainer: DelayedContainer, data: ByteArray)
    fun deserializeCloudioLog(cloudioLogMessage: CloudioLogMessage, data: ByteArray)
    fun serializeLogParameter(logParameter: LogParameter): ByteArray
    fun deserializeLogParameter(logParameter: LogParameter, data: ByteArray)
    fun serializeJobParameter(jobParameter: JobParameter): ByteArray
    fun deserializeJobsLineOutput(jobsLineOutput: JobsLineOutput, data: ByteArray)
}

fun Collection<SerializationFormat>.detect(data: ByteArray) = this.firstOrNull() { it.detect(data) }

fun Collection<SerializationFormat>.fromIdentifiers(identifiers: Collection<String>) = this.firstOrNull {
    (if (identifiers.isEmpty()) listOf("JSON") else identifiers).contains(it.identifier())
}
