package ch.hevs.cloudio.cloud.abstractservices.messaging

import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractTopicService(private val topics: Set<String>) : RabbitListenerConfigurer, MessageListener {
    private val log = LogFactory.getLog(AbstractTopicService::class.java)

    @Autowired
    private lateinit var amqpAdmin: AmqpAdmin

    constructor(topic: String) : this(setOf(topic))

    final override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        if (topics.isNotEmpty()) {
            val exchange = TopicExchange("amq.topic")
            val queue = Queue(javaClass.canonicalName)
            amqpAdmin.declareQueue(queue)
            topics.forEach {
                amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(it))
            }
            val endpoint = SimpleRabbitListenerEndpoint()
            endpoint.id = this.javaClass.canonicalName
            endpoint.setQueues(queue)
            endpoint.messageListener = this
            registrar.registerEndpoint(endpoint)
        }
    }

    final override fun onMessage(message: Message) {
        try {
            handleMessage(message)
        } catch (exception: Exception) {
            log.error("Exception during message handling (${topics.joinToString(",")}):", exception)
        }
    }

    abstract fun handleMessage(message: Message)
}
