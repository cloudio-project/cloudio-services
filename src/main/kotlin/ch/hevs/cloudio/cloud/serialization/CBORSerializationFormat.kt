package ch.hevs.cloudio.cloud.serialization

import ch.hevs.cloudio.cloud.model.*
import org.springframework.stereotype.Component

@Component
class CBORSerializationFormat: SerializationFormat {
    override fun detect(data: ByteArray): Boolean {
        return false
        // TODO("Not yet implemented")
    }

    override fun deserializeEndpoint(endpoint: Endpoint, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun deserializeNode(node: Node, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun deserializeObject(obj: CloudioObject, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun serializeAttribute(attribute: Attribute): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deserializeAttribute(attribute: Attribute, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun deserializeTransaction(transaction: Transaction, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun deserializeDelayed(delayedContainer: DelayedContainer, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun deserializeCloudioLog(cloudioLogMessage: CloudioLogMessage, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun serializeLogParameter(logParameter: LogParameter): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deserializeLogParameter(logParameter: LogParameter, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun serializeJobParameter(jobParameter: JobParameter): ByteArray {
        TODO("Not yet implemented")
    }

    override fun deserializeJobsLineOutput(jobsLineOutput: JobsLineOutput, data: ByteArray) {
        TODO("Not yet implemented")
    }
}