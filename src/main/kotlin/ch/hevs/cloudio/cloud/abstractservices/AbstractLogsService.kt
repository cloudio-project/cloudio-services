package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogMessage
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractLogsService(private val serializationFormats: Collection<SerializationFormat>) {

    companion object {
        private val log = LogFactory.getLog(AbstractLogsService::class.java)
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@logsLevel.#"]
        )
    ])
    fun handleLogLevelMessage(message: Message) {
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

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@logs.#"]
        )
    ])
    fun handleLogsMessage(message: Message) {
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
