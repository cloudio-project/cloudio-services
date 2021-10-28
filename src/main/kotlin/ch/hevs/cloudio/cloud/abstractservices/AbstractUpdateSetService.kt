package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractUpdateSetService: RabbitListenerConfigurer {

    @Autowired private lateinit var amqpAdmin: AmqpAdmin
    @Autowired private lateinit var serializationFormats: Collection<SerializationFormat>

    companion object {
        private val log = LogFactory.getLog(AbstractUpdateSetService::class.java)
    }

    override fun configureRabbitListeners(registrar: RabbitListenerEndpointRegistrar) {
        val exchange = TopicExchange("amq.topic")

        // Update message handling.
        val updateQueue = Queue("${javaClass.canonicalName}-update")
        amqpAdmin.declareQueue(updateQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(updateQueue).to(exchange).with("@update.#"))
        val updateEndpoint = SimpleRabbitListenerEndpoint()
        updateEndpoint.id = "${javaClass.canonicalName}-update"
        updateEndpoint.setQueues(updateQueue)
        updateEndpoint.messageListener = MessageListener { message -> handleUpdateOrSetMessage(message) }
        registrar.registerEndpoint(updateEndpoint)

        // Set message handling.
        val setQueue = Queue("${javaClass.canonicalName}-set")
        amqpAdmin.declareQueue(setQueue)
        amqpAdmin.declareBinding(BindingBuilder.bind(setQueue).to(exchange).with("@set.#"))
        val setEndpoint = SimpleRabbitListenerEndpoint()
        setEndpoint.id = "${javaClass.canonicalName}-set"
        setEndpoint.setQueues(setQueue)
        setEndpoint.messageListener = MessageListener { message -> handleUpdateOrSetMessage(message) }
        registrar.registerEndpoint(setEndpoint)
    }

    private fun handleUpdateOrSetMessage(message: Message) {
        val prefix = message.messageProperties.receivedRoutingKey.split(".")[0]
        try {
            val attributeTopic = message.messageProperties.receivedRoutingKey.removePrefix("$prefix.")

            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val attribute = messageFormat.deserializeAttribute(data)
                if (attribute.timestamp != null && attribute.timestamp != -1.0 && attribute.value != null) {

                    if (prefix.equals("@update") && (attribute.constraint == AttributeConstraint.Measure || attribute.constraint == AttributeConstraint.Status) ||
                            (prefix.equals("@set") && (attribute.constraint == AttributeConstraint.Parameter || attribute.constraint == AttributeConstraint.SetPoint))) {

                        var attributeId = ""
                        val splitAttributeId = attributeTopic.split(".")

                        //check if topic follow pattern of cloud.iO v0.1 or v0.2
                        if(splitAttributeId[1] == "nodes" && splitAttributeId[3] == "objects" && splitAttributeId[splitAttributeId.lastIndex-1] == "attributes" ) {

                            splitAttributeId.forEachIndexed { i, topicPart ->
                                when {
                                    i % 2 == 0 -> attributeId += topicPart
                                    i != splitAttributeId.lastIndex -> attributeId += "."
                                }
                            }
                        }else
                            attributeId = attributeTopic
                        attributeUpdatedSet(attributeId, attribute, prefix)
                    } else {
                        log.error("The Attribute $attributeTopic with the constraint ${attribute.constraint} can't be changed with the prefix $prefix")
                    }
                } else {
                    log.error("The Attribute $attributeTopic has be $prefix with a timestamp of -1 0r value of null")
                }
            } else {
                log.error("Unrecognized message format in $prefix message from $attributeTopic")
            }
        } catch (e: Exception) {
            log.error("Exception during $prefix message handling:", e)
        }
    }

    // Abstract method to handle update of message.
    abstract fun attributeUpdatedSet(attributeId: String, attribute: Attribute, prefix: String)
}
