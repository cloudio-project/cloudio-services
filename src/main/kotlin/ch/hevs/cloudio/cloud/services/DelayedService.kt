package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractDelayedService
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("delayed", "default")
class DelayedService(
        private val influxAttributeService: InfluxAttributeService,
        private val influxLogService: InfluxLogService,
        private val rabbitTemplate: RabbitTemplate
): AbstractDelayedService() {
    private val log = LogFactory.getLog(DelayedService::class.java)

    override fun delayedMessages(id: ModelIdentifier, messages: DelayedMessages, messageFormat: SerializationFormat) {
        messages.messages.forEach { delayedMessage ->
            val messageId = ModelIdentifier(delayedMessage.topic)
            when (messageId.action) {
                ActionIdentifier.ATTRIBUTE_UPDATE -> {
                    (delayedMessage.data as? Attribute)?.let {
                        handleUpdate(messageId, it)
                    }
                }
                ActionIdentifier.TRANSACTION -> {
                    (delayedMessage.data as? Transaction)?.let {
                        handleTransaction(messageId, it, messageFormat)
                    }
                }
                ActionIdentifier.LOG_OUTPUT -> {
                    (delayedMessage.data as? LogMessage)?.let {
                        handleLogMessage(messageId.endpoint, it)
                    }
                }
                else -> {
                    log.error(
                        "Unrecognized message with sub-topic ${delayedMessage.topic} " +
                                "inside @delayed message from $id"
                    )
                }
            }
        }
    }

    fun handleUpdate(id: ModelIdentifier, attribute: Attribute) {
        influxAttributeService.attributeUpdated(id, attribute)
    }

    fun handleTransaction(id: ModelIdentifier, transaction: Transaction, messageFormat: SerializationFormat) {
        transaction.attributes.filter { (topic, _ ) -> ModelIdentifier(topic).endpoint == id.endpoint }.forEach { (topic, attribute) ->
            rabbitTemplate.convertAndSend(
                "amq.topic",
                topic,
                messageFormat.serializeAttribute(attribute)
            )
        }
    }

    fun handleLogMessage(endpointUuid: UUID, logMessage: LogMessage) {
        influxLogService.logMessage(endpointUuid, logMessage)
    }
}
