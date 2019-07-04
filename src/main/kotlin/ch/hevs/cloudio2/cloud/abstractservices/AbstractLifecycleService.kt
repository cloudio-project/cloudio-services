package ch.hevs.cloudio2.cloud.abstractservices

import ch.hevs.cloudio2.cloud.model.Endpoint
import ch.hevs.cloudio2.cloud.model.Node
import ch.hevs.cloudio2.cloud.serialization.JsonSerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractLifecycleService{

    companion object {
        private val log = LogFactory.getLog(AbstractLifecycleService::class.java)
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@online.*"])])
    fun handleOnlineMessage(message: Message)
    {
        log.info("@online.*")

        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = JsonSerializationFormat.detect(data)
            if (messageFormat) {
                val endpoint = Endpoint()
                JsonSerializationFormat.deserializeEndpoint(endpoint, data)
                endpointIsOnline(endpointId, endpoint)
            } else {
                log.error("Unrecognized message format in @online message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @online message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@offline.*"])])
    fun handleOfflineMessage(message: Message)
    {
        log.info("@offline.*")

        try {
            endpointIsOffline(message.messageProperties.receivedRoutingKey.split(".")[1])
        } catch (exception: Exception) {
            log.error("Exception during @offline message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@nodeAdded.*.nodes.*"])])
    fun handleNodeAddedMessage(message: Message)
    {
        log.info("@nodeAdded.*.nodes.*")
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val nodeName = message.messageProperties.receivedRoutingKey.split(".")[3]
            val data = message.body
            val messageFormat = JsonSerializationFormat.detect(data)
            if (messageFormat) {
                val node = Node()
                JsonSerializationFormat.deserializeNode(node, data)
                nodeAdded(endpointId, nodeName, node)
            } else {
                log.error("Unrecognized message format in @nodeAdded message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@nodeRemoved.*.nodes.*"])])
    fun handleNodeRemovedMessage(message: Message)
    {
        log.info("@nodeRemoved.*.nodes.*")

        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val nodeName = message.messageProperties.receivedRoutingKey.split(".")[3]
            nodeRemoved(endpointId, nodeName)
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    abstract fun endpointIsOnline(endpointId: String, endpoint: Endpoint)
    abstract fun endpointIsOffline(endpointId: String)
    abstract fun nodeAdded(endpointId: String, nodeName: String, node: Node)
    abstract fun nodeRemoved(endpointId: String, nodeName: String)

}
