package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractLifecycleService: RabbitListenerConfigurer {

    @Autowired private lateinit var amqpAdmin: AmqpAdmin
    @Autowired private lateinit var serializationFormats: Collection<SerializationFormat>

    companion object {
        private val log = LogFactory.getLog(AbstractLifecycleService::class.java)
    }

    override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        val exchange = TopicExchange("amq.topic")

        // Online message handling.
        val onlineQueue = Queue("${javaClass.canonicalName}-online")
        amqpAdmin.declareQueue(onlineQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(onlineQueue).to(exchange).with("@online.*"))
        val onlineEndpoint = SimpleRabbitListenerEndpoint()
        onlineEndpoint.id = "${javaClass.canonicalName}-online"
        onlineEndpoint.setQueues(onlineQueue)
        onlineEndpoint.messageListener = MessageListener { message -> handleOnlineMessage(message) }
        registrar.registerEndpoint(onlineEndpoint)

        // Offline message handling.
        val offlineQueue = Queue("${javaClass.canonicalName}-offline")
        amqpAdmin.declareQueue(offlineQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(offlineQueue).to(exchange).with("@offline.*"))
        val offlineEndpoint = SimpleRabbitListenerEndpoint()
        offlineEndpoint.id = "${javaClass.canonicalName}-offline"
        offlineEndpoint.setQueues(offlineQueue)
        offlineEndpoint.messageListener = MessageListener { message -> handleOfflineMessage(message) }
        registrar.registerEndpoint(offlineEndpoint)

        // Node added message handling.
        val nodeAddedQueue = Queue("${javaClass.canonicalName}-node-added")
        amqpAdmin.declareQueue(nodeAddedQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(nodeAddedQueue).to(exchange).with("@nodeAdded.*.*"))
        amqpAdmin.declareBinding(BindingBuilder.bind(nodeAddedQueue).to(exchange).with("@nodeAdded.*.nodes.*"))
        val nodeAddedEndpoint = SimpleRabbitListenerEndpoint()
        nodeAddedEndpoint.id = "${javaClass.canonicalName}-node-added"
        nodeAddedEndpoint.setQueues(nodeAddedQueue)
        nodeAddedEndpoint.messageListener = MessageListener { message -> handleNodeAddedMessage(message) }
        registrar.registerEndpoint(nodeAddedEndpoint)

        // Node removed message handling.
        val nodeRemovedQueue = Queue("${javaClass.canonicalName}-node-removed")
        amqpAdmin.declareQueue(nodeRemovedQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(nodeRemovedQueue).to(exchange).with("@nodeRemoved.*.*"))
        amqpAdmin.declareBinding(BindingBuilder.bind(nodeRemovedQueue).to(exchange).with("@nodeRemoved.*.nodes.*"))
        val nodeRemovedEndpoint = SimpleRabbitListenerEndpoint()
        nodeRemovedEndpoint.id = "${javaClass.canonicalName}-node-removed"
        nodeRemovedEndpoint.setQueues(nodeRemovedQueue)
        nodeRemovedEndpoint.messageListener = MessageListener { message -> handleNodeRemovedMessage(message) }
        registrar.registerEndpoint(nodeRemovedEndpoint)
    }

    private fun handleOnlineMessage(message: Message) {
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val endpoint = messageFormat.deserializeEndpointDataModel(data)
                endpointIsOnline(endpointId, endpoint)
            } else {
                log.error("Unrecognized message format in @online message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @online message handling:", exception)
        }
    }

    private fun handleOfflineMessage(message: Message) {
        try {
            endpointIsOffline(message.messageProperties.receivedRoutingKey.split(".")[1])
        } catch (exception: Exception) {
            log.error("Exception during @offline message handling:", exception)
        }
    }

    private fun handleNodeAddedMessage(message: Message) {
        try {
            val splitTopic = message.messageProperties.receivedRoutingKey.split(".")
            val endpointId = splitTopic[1]
            val nodeName =  message.messageProperties.receivedRoutingKey.split(".")[splitTopic.lastIndex]
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val node = messageFormat.deserializeNode(data)
                nodeAdded(endpointId, nodeName, node)
            } else {
                log.error("Unrecognized message format in @nodeAdded message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    private fun handleNodeRemovedMessage(message: Message) {
        try {
            val splitTopic = message.messageProperties.receivedRoutingKey.split(".")
            val endpointId = splitTopic[1]
            val nodeName =  message.messageProperties.receivedRoutingKey.split(".")[splitTopic.lastIndex]
            nodeRemoved(endpointId, nodeName)
        } catch (exception: Exception) {
            log.error("Exception during @nodeAdded message handling:", exception)
        }
    }

    abstract fun endpointIsOnline(uuid: String, dataModel: EndpointDataModel)
    abstract fun endpointIsOffline(uuid: String)
    abstract fun nodeAdded(uuid: String, nodeName: String, node: Node)
    abstract fun nodeRemoved(uuid: String, nodeName: String)
}
