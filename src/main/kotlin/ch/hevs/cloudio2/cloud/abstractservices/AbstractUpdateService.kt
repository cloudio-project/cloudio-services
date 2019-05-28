package ch.hevs.cloudio2.cloud.abstractservices

import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.serialization.SerializationFormatFactory
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractUpdateService{

    companion object {
        private val log = LoggerFactory.getLogger(AbstractUpdateService::class.java)
    }

    @RabbitListener(
            bindings = [QueueBinding(value= Queue(),
            exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = ["@update.#"])])
    fun handleUpdateMessage(message: Message)
    {
        log.info("@update.#")

        val attributeId = message.messageProperties.receivedRoutingKey.removePrefix("@update.")

        val data = message.body
        val messageFormat = SerializationFormatFactory.serializationFormat(data)
        if (messageFormat != null) {
            val attribute = Attribute()
            messageFormat.deserializeAttribute(attribute, data)
            if (attribute.timestamp != -1.0 && attribute.value != null) {
                attributeUpdated(attributeId, attribute)
            }
        } else {
            log.error("Unrecognized message format in @update message from $attributeId")
        }
    }

    //Abstract methode to handle update of message
    abstract fun attributeUpdated(attributeId: String, attribute: Attribute)
}