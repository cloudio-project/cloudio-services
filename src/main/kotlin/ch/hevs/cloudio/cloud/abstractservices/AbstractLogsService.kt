package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogMessage
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractLogsService: RabbitListenerConfigurer {

    @Autowired private lateinit var amqpAdmin: AmqpAdmin
    @Autowired private lateinit var serializationFormats: Collection<SerializationFormat>

    companion object {
        private val log = LogFactory.getLog(AbstractLogsService::class.java)
    }

    override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        val exchange = TopicExchange("amq.topic")

        // Log level message handling.
        val logLevelQueue = Queue("${javaClass.canonicalName}-log-level")
        amqpAdmin.declareQueue(logLevelQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(logLevelQueue).to(exchange).with("@logsLevel.#"))
        val logLevelEndpoint = SimpleRabbitListenerEndpoint()
        logLevelEndpoint.id = "${javaClass.canonicalName}-log-level"
        logLevelEndpoint.setQueues(logLevelQueue)
        logLevelEndpoint.messageListener = MessageListener { message -> handleLogLevelMessage(message) }
        registrar.registerEndpoint(logLevelEndpoint)

        // Log message handling.
        val logQueue = Queue("${javaClass.canonicalName}-log")
        amqpAdmin.declareQueue(logQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(logQueue).to(exchange).with("@logs.#"))
        val logEndpoint = SimpleRabbitListenerEndpoint()
        logEndpoint.id = "${javaClass.canonicalName}-log"
        logEndpoint.setQueues(logQueue)
        logEndpoint.messageListener = MessageListener { message -> handleLogsMessage(message) }
        registrar.registerEndpoint(logEndpoint)
    }

    private fun handleLogLevelMessage(message: Message) {
        try {
            val endpointUuid = message.messageProperties.receivedRoutingKey.removePrefix("@logsLevel.")

            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                logLevelChanged(endpointUuid, messageFormat.deserializeLogLevel(data))
            } else {
                log.error("Unrecognized message format in @logsLevel message from $endpointUuid")
            }
        } catch (exception: Exception) {
            log.error("Exception during @logsLevel message handling:", exception)
        }
    }

    private fun handleLogsMessage(message: Message) {
        try {
            val endpointUuid = message.messageProperties.receivedRoutingKey.removePrefix("@logs.")

            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val logMessage = messageFormat.deserializeLogMessage(data)
                if (logMessage.timestamp != -1.0)
                    newLog(endpointUuid, logMessage)
            } else {
                log.error("Unrecognized message format in @logs message from $endpointUuid")
            }

        } catch (exception: Exception) {
            log.error("Exception during @logs message handling:", exception)
        }
    }

    // Abstract method to handle log level change messages.
    abstract fun logLevelChanged(endpointUuid: String, logLevel: LogLevel)

    // Abstract method to handle logs messages.
    abstract fun newLog(endpointUuid: String, logMessage: LogMessage)
}
