package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

abstract class AbstractLifecycleService : AbstractTopicService(
    setOf(
        "${ActionIdentifier.ENDPOINT_ONLINE}.*",
        "${ActionIdentifier.ENDPOINT_OFFLINE}.*",
        "${ActionIdentifier.NODE_ADDED}.*.*",
        "${ActionIdentifier.NODE_ADDED}.*.nodes.*",
        "${ActionIdentifier.NODE_REMOVED}.*.*",
        "${ActionIdentifier.NODE_REMOVED}.*.nodes.*"
    )
) {

    @Autowired
    private lateinit var serializationFormats: Collection<SerializationFormat>

    companion object {
        private val log = LogFactory.getLog(AbstractLifecycleService::class.java)
    }

    final override fun handleMessage(message: Message) {
        val id = ModelIdentifier(message.messageProperties.receivedRoutingKey)
        if (id.valid) {
            when {
                id.action == ActionIdentifier.ENDPOINT_ONLINE && id.count() == 0 -> handleEndpointOnlineMessage(id.endpoint, message)
                id.action == ActionIdentifier.ENDPOINT_OFFLINE && id.count() == 0 -> handleEndpointOfflineMessage(id.endpoint)
                id.action == ActionIdentifier.NODE_ADDED -> handleNodeAddedMessage(id, message)
                id.action == ActionIdentifier.NODE_REMOVED -> handleNodeRemovedMessage(id)
                else -> log.error("Unexpected action or invalid topic: $id")
            }
        } else {
            log.error("Invalid topic: ${message.messageProperties.receivedRoutingKey}")
        }
    }

    private fun handleEndpointOnlineMessage(endpointUUID: UUID, message: Message) {
        val data = message.body
        val messageFormat = serializationFormats.detect(data)
        if (messageFormat != null) {
            val endpoint = messageFormat.deserializeEndpointDataModel(data)
            endpointIsOnline(endpointUUID, endpoint)
        } else {
            log.error("Unrecognized message format in @online message from $endpointUUID")
        }
    }

    private fun handleEndpointOfflineMessage(endpointUUID: UUID) {
        endpointIsOffline(endpointUUID)
    }

    private fun handleNodeAddedMessage(id: ModelIdentifier, message: Message) {
        if (id.count() == 1) {
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val node = messageFormat.deserializeNode(data)
                nodeAdded(id.endpoint, id[0], node)
            } else {
                log.error("Unrecognized message format in @nodeAdded message from ${id.endpoint}")
            }
        } else if (id.count() == 2) {
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val node = messageFormat.deserializeNode(data)
                nodeAdded(id.endpoint, id[1], node)
            } else {
                log.error("Unrecognized message format in @nodeAdded message from ${id.endpoint}")
            }
        } else {
            log.error("Invalid topic for @nodeAdded action: $id")
        }
    }

    private fun handleNodeRemovedMessage(id: ModelIdentifier) {
        if (id.count() == 1) {
            nodeRemoved(id.endpoint, id[0])
        } else if (id.count() == 2) {
            nodeRemoved(id.endpoint, id[1])
        } else {
            log.error("Invalid topic for @nodeAdded action: $id")
        }
    }

    abstract fun endpointIsOnline(endpointUUID: UUID, dataModel: EndpointDataModel)
    abstract fun endpointIsOffline(endpointUUID: UUID)
    abstract fun nodeAdded(endpointUUID: UUID, nodeName: String, node: Node)
    abstract fun nodeRemoved(endpointUUID: UUID, nodeName: String)
}
