package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractSimpleTopicService
import ch.hevs.cloudio.cloud.abstractservices.annotation.TopicListener
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("transaction", "default")
@TopicListener(topics = ["@transaction.*"])
class EndpointTransactionService(
        private val serializationFormats: Collection<SerializationFormat>,
        private val rabbitTemplate: RabbitTemplate
): AbstractSimpleTopicService() {
    private val log = LogFactory.getLog(EndpointTransactionService::class.java)

    override fun handleMessage(message: Message) {
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val transaction = messageFormat.deserializeTransaction(data)
                transaction.attributes.forEach { (topic, attribute) ->
                    rabbitTemplate.convertAndSend("amq.topic",
                            "@update.${topic.replace("/", ".")}",
                            messageFormat.serializeAttribute(attribute))
                }
            } else {
                log.error("Unrecognized message format in @transaction message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @transaction message handling:", exception)
        }
    }
}
