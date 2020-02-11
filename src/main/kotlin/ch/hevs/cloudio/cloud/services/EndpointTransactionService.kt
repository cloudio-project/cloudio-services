package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.model.Transaction
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("transaction", "default")
class EndpointTransactionService(private val rabbitTemplate: RabbitTemplate) {
    private val log = LogFactory.getLog(EndpointTransactionService::class.java)

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(),
                exchange = Exchange(
                        value = "amq.topic",
                        type = ExchangeTypes.TOPIC,
                        ignoreDeclarationExceptions = "true"
                ),
                key = ["@transaction.*"]
        )
    ])
    fun handleTransactionMessage(message: Message) {
        try {
            val endpointId = message.messageProperties.receivedRoutingKey.split(".")[1]
            val data = message.body
            val messageFormat = JsonSerializationFormat.detect(data)
            if (messageFormat) {
                val transaction = Transaction()
                JsonSerializationFormat.deserializeTransaction(transaction, data)

                transaction.attributes.forEach { (topic, attribute) ->
                    rabbitTemplate.convertAndSend("amq.topic",
                            "@update.${topic.replace("/", ".")}",
                            JsonSerializationFormat.serializeAttribute(attribute))
                }
            } else {
                log.error("Unrecognized message format in @transaction message from $endpointId")
            }
        } catch (exception: Exception) {
            log.error("Exception during @transaction message handling:", exception)
        }
    }
}
