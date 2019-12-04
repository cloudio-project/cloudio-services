package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.serialization.JsonSerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener

abstract class AbstractUpdateSetService {

    companion object {
        private val log = LogFactory.getLog(AbstractUpdateSetService::class.java)
    }

    @RabbitListener(
            bindings = [QueueBinding(value = Queue(),
                    exchange = Exchange(value = "amq.topic", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
                    key = ["@update.#", "@set.#"])])
    fun handleUpdateMessage(message: Message) {
        val prefix = message.messageProperties.receivedRoutingKey.split(".")[0]
        try {
            val attributeId = message.messageProperties.receivedRoutingKey.removePrefix("$prefix.")

            val data = message.body
            val messageFormat = JsonSerializationFormat.detect(data)
            if (messageFormat) {
                val attribute = Attribute()
                JsonSerializationFormat.deserializeAttribute(attribute, data)
                if (attribute.timestamp != -1.0 && attribute.value != null) {

                    if (prefix.equals("@update") && (attribute.constraint == AttributeConstraint.Measure || attribute.constraint == AttributeConstraint.Status) ||
                            (prefix.equals("@set") && (attribute.constraint == AttributeConstraint.Parameter || attribute.constraint == AttributeConstraint.SetPoint))) {
                        attributeUpdatedSet(attributeId, attribute, prefix)
                    } else {
                        log.error("The Attribute $attributeId with the constraint ${attribute.constraint} can't be changed with the prefix $prefix")
                    }
                } else {
                    log.error("The Attribute $attributeId has be $prefix with a timestamp of -1 0r value of null")
                }
            } else {
                log.error("Unrecognized message format in $prefix message from $attributeId")
            }
        } catch (e: Exception) {
            log.error("Exception during $prefix message handling:", e)
        }
    }

    //Abstract method to handle update of message
    abstract fun attributeUpdatedSet(attributeId: String, attribute: Attribute, prefix: String)
}
