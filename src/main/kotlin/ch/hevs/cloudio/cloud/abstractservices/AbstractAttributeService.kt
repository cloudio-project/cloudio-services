package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractAttributeService : AbstractTopicService(
    setOf(
        "${ActionIdentifier.ATTRIBUTE_UPDATE}.#",
        "${ActionIdentifier.ATTRIBUTE_SET}.#",
        "${ActionIdentifier.ATTRIBUTE_DID_SET}.#"
    )
) {
    private val log = LogFactory.getLog(AbstractAttributeService::class.java)

    @Autowired
    private lateinit var serializationFormats: Collection<SerializationFormat>

    final override fun handleMessage(message: Message) {
        val id = ModelIdentifier(message.messageProperties.receivedRoutingKey)
        if (id.valid) {
            when (id.action) {
                ActionIdentifier.ATTRIBUTE_UPDATE -> handleAttributeUpdateOrSetMessage(id, message)
                ActionIdentifier.ATTRIBUTE_SET -> handleAttributeUpdateOrSetMessage(id, message)
                ActionIdentifier.ATTRIBUTE_DID_SET -> handleAttributeDidSetMessage(id, message)
                else -> log.error("Unexpected action: ${id.action}")
            }
        } else {
            log.error("Invalid topic: ${message.messageProperties.receivedRoutingKey}")
        }
    }

    private fun handleAttributeUpdateOrSetMessage(id: ModelIdentifier, message: Message) {
        val action = id.action
            message.messageProperties.receivedRoutingKey.split(".")[0]
        try {
            val attributeTopic = message.messageProperties.receivedRoutingKey.removePrefix("$action.")

            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val attribute = messageFormat.deserializeAttribute(data)
                if (attribute.timestamp != null && attribute.timestamp != -1.0 && attribute.value != null) {

                    if (action == ActionIdentifier.ATTRIBUTE_UPDATE && (attribute.constraint == AttributeConstraint.Measure || attribute.constraint == AttributeConstraint.Status) ||
                        (action == ActionIdentifier.ATTRIBUTE_SET && (attribute.constraint == AttributeConstraint.Parameter || attribute.constraint == AttributeConstraint.SetPoint))
                    ) {

                        var attributeId = ""
                        val splitAttributeId = attributeTopic.split(".")

                        //check if topic follow pattern of cloud.iO v0.1 or v0.2
                        if (splitAttributeId[1] == "nodes" && splitAttributeId[3] == "objects" && splitAttributeId[splitAttributeId.lastIndex - 1] == "attributes") {

                            splitAttributeId.forEachIndexed { i, topicPart ->
                                when {
                                    i % 2 == 0 -> attributeId += topicPart
                                    i != splitAttributeId.lastIndex -> attributeId += "."
                                }
                            }
                        } else
                            attributeId = attributeTopic
                        when (action) {
                            ActionIdentifier.ATTRIBUTE_UPDATE -> attributeUpdated(attributeId, attribute)
                            ActionIdentifier.ATTRIBUTE_SET -> attributeSet(attributeId, attribute)
                            else -> log.error("Unexpected action: ${id.action}")
                        }
                    } else {
                        log.error("The Attribute $attributeTopic with the constraint ${attribute.constraint} can't be changed with the prefix $action")
                    }
                } else {
                    log.error("The Attribute $attributeTopic has be $action with a timestamp of -1 0r value of null")
                }
            } else {
                log.error("Unrecognized message format in $action message from $attributeTopic")
            }
        } catch (e: Exception) {
            log.error("Exception during $action message handling:", e)
        }
    }

    private fun handleAttributeDidSetMessage(id: ModelIdentifier, message: Message) {
        // TODO: Implement.
    }

    // Abstract method to handle update of attribute.
    abstract fun attributeUpdated(attributeId: String, attribute: Attribute)

    // Abstract method to handle set of attribute.
    abstract fun attributeSet(attributeId: String, attribute: Attribute)

    // Abstract method to handel didSet of attribute.
    abstract fun attributeDidSet(attributeId: String, attribute: Attribute)
}
