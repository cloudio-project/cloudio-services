package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.DelayedMessages
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractDelayedService: AbstractTopicService("${ActionIdentifier.DELAYED_MESSAGES}.*") {
    private val log = LogFactory.getLog(AbstractDelayedService::class.java)

    @Autowired
    private lateinit var serializationFormats: Collection<SerializationFormat>

    override fun handleMessage(message: Message) {
        try {
            val id = ModelIdentifier(message.messageProperties.receivedRoutingKey)
            if (id.valid && id.count() == 0) {
                val data = message.body
                val messageFormat = serializationFormats.detect(data)
                if (messageFormat != null) {
                    delayedMessages(id, messageFormat.deserializeDelayedMessages(data), messageFormat)
                } else {
                    log.error("Unrecognized message format in @delayed message from $id")
                }
            } else {
                log.error("Invalid topic: ${message.messageProperties.receivedRoutingKey}")
            }
        } catch (e: Exception) {
        log.error("Exception during @delayed message handling:", e)
    }
    }

    abstract fun delayedMessages(id: ModelIdentifier, messages: DelayedMessages, messageFormat: SerializationFormat)
}