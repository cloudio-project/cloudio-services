package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractLifecycleService(private val serializationFormats: Collection<SerializationFormat>) {

    companion object {
        private val log = LogFactory.getLog(AbstractLifecycleService::class.java)
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@online.*"]
        )
    ])
    fun handleOnlineMessage(message: Message) {
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val endpoint = EndpointDataModel(version = "v0.1")
                messageFormat.deserializeEndpoint(endpoint, data)
                endpointIsOnline(endpointId, endpoint)
            } else {
                log.error("Unrecognized message format in @online message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @online message handling:", exception)
        }
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@offline.*"]
        )
    ])
    fun handleOfflineMessage(message: Message) {
        try {
            endpointIsOffline(message.messageProperties.receivedRoutingKey.split(".")[1])
        } catch (exception: Exception) {
            log.error("Exception during @offline message handling:", exception)
        }
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@nodeAdded.*.*","@nodeAdded.*.nodes.*"]
        )
    ])
    fun handleNodeAddedMessage(message: Message) {
        try {
            val splitTopic = message.messageProperties.receivedRoutingKey.split(".")
            val endpointId = splitTopic[1]
            val nodeName =  message.messageProperties.receivedRoutingKey.split(".")[splitTopic.lastIndex]
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val node = Node()
                messageFormat.deserializeNode(node, data)
                nodeAdded(endpointId, nodeName, node)
            } else {
                log.error("Unrecognized message format in @nodeAdded message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"),
                key = ["@nodeRemoved.*.*","@nodeRemoved.*.nodes.*"]
        )
    ])
    fun handleNodeRemovedMessage(message: Message) {
        try {
            val splitTopic = message.messageProperties.receivedRoutingKey.split(".")
            val endpointId = splitTopic[1]
            val nodeName =  message.messageProperties.receivedRoutingKey.split(".")[splitTopic.lastIndex]
            nodeRemoved(endpointId, nodeName)
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    abstract fun endpointIsOnline(endpointId: String, endpoint: EndpointDataModel)
    abstract fun endpointIsOffline(endpointId: String)
    abstract fun nodeAdded(endpointId: String, nodeName: String, node: Node)
    abstract fun nodeRemoved(endpointId: String, nodeName: String)
}
