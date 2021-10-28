package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.abstractservices.annotation.TopicListener
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractSimpleTopicService: RabbitListenerConfigurer, MessageListener {

    companion object {
        private val log = LogFactory.getLog(AbstractLogsService::class.java)
    }

    @Autowired private lateinit var amqpAdmin: AmqpAdmin

    override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        javaClass.getAnnotation(TopicListener::class.java)?.let { service ->
            if (service.topics.isNotEmpty()) {
                val exchange = TopicExchange("amq.topic")
                val queue = Queue(javaClass.canonicalName)
                amqpAdmin.declareQueue(queue)
                service.topics.forEach {
                    amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(it))
                }
                val endpoint = SimpleRabbitListenerEndpoint()
                endpoint.id = this.javaClass.canonicalName
                endpoint.setQueues(queue)
                endpoint.messageListener = this
                registrar.registerEndpoint(endpoint)
            }
        }
    }

    override fun onMessage(message: Message) {
        try {
            handleMessage(message)
        } catch (exception: Exception) {
            log.error("Exception during message handling (${javaClass.getAnnotation(TopicListener::class.java)?.topics?.joinToString(",") ?: "-"}):", exception)
        }
    }

    abstract fun handleMessage(message: Message)
}
