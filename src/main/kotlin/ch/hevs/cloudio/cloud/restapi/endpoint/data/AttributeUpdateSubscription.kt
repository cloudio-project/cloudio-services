package ch.hevs.cloudio.cloud.restapi.endpoint.data

import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

class AttributeUpdateSubscription(ids: List<ModelIdentifier>, timeout: Long, private val serializationFormats: Collection<SerializationFormat>,
                                  amqp: AmqpAdmin, connectionFactory: ConnectionFactory): MessageListener, SseEmitter(timeout) {
    private val container = SimpleMessageListenerContainer()
    init {
        val queue = amqp.declareQueue()
        val exchange = TopicExchange("amq.topic")
        ids.forEach {
            amqp.declareBinding(BindingBuilder.bind(queue).to(exchange).with(it.toAMQPTopic()))
        }
        container.connectionFactory = connectionFactory
        container.setMessageListener(this)
        container.addQueues(queue)
        onTimeout { container.stop() }
        onCompletion { container.stop() }
        onError { container.stop() }
        container.start()
    }

    override fun onMessage(message: Message) {
        try {
            val id = ModelIdentifier(message.messageProperties.receivedRoutingKey)
            if (id.valid && id.action == ActionIdentifier.ATTRIBUTE_UPDATE) {
                val data = message.body
                val messageFormat = serializationFormats.detect(data)
                if (messageFormat != null) {
                    val attribute = messageFormat.deserializeAttribute(data)
                    send(AttributeUpdateEvent(id.toString('/'),  attribute))
                }
            }
        } catch (e: Exception) { }
    }
}
