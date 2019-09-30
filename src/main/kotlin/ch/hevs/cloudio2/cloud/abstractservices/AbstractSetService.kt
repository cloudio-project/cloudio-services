package ch.hevs.cloudio2.cloud.abstractservices

import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.serialization.JsonSerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractSetService{

    companion object {
        private val log = LogFactory.getLog(AbstractSetService::class.java)
    }

    @RabbitListener(
            bindings = [QueueBinding(value= Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@set.#"])])
    fun handleSetMessage(message: Message)
    {
        log.info("@set.#")

        val attributeId = message.messageProperties.receivedRoutingKey.removePrefix("@set.")

        val data = message.body
        val messageFormat = JsonSerializationFormat.detect(data)
        if (messageFormat) {
            val attribute = Attribute()
            JsonSerializationFormat.deserializeAttribute(attribute, data)
            if (attribute.timestamp != -1.0 && attribute.value != null) {
                attributeSet(attributeId, attribute)
            }
        } else {
            log.error("Unrecognized message format in @set message from $attributeId")
        }
    }

    //Abstract method to handle update of message
    abstract fun attributeSet(attributeId: String, attribute: Attribute)
}
