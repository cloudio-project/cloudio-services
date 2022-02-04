package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.model.*
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
                ActionIdentifier.ATTRIBUTE_UPDATE -> handleAttributeUpdateMessage(id, message)
                ActionIdentifier.ATTRIBUTE_SET -> handleAttributeSetMessage(id, message)
                ActionIdentifier.ATTRIBUTE_DID_SET -> handleAttributeDidSetMessage(id, message)
                else -> log.error("Unexpected action: ${id.action}")
            }
        } else {
            log.error("Invalid topic: ${message.messageProperties.receivedRoutingKey}")
        }
    }

    private fun handleAttributeUpdateMessage(id: ModelIdentifier, message: Message) {
        try {
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val attribute = messageFormat.deserializeAttribute(data)
                if (attribute.timestamp != null && attribute.timestamp != -1.0 && attribute.value != null) {
                    if (id.action == ActionIdentifier.ATTRIBUTE_UPDATE && (attribute.constraint == AttributeConstraint.Measure || attribute.constraint == AttributeConstraint.Status)
                    ) {
                        attributeUpdated(id, attribute)
                    } else {
                        log.error("The Attribute $id with the constraint ${attribute.constraint} can't be changed with the prefix ${id.action}")
                    }
                } else {
                    log.error("The Attribute $id has be ${id.action} with a timestamp of -1 0r value of null")
                }
            } else {
                log.error("Unrecognized message format in ${id.action} message from $id")
            }
        } catch (e: Exception) {
            log.error("Exception during ${id.action} message handling:", e)
        }
    }

    private fun handleAttributeSetMessage(id: ModelIdentifier, message: Message) {
        try {
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val attribute = messageFormat.deserializeAttributeSetCommand(data)
                if (attribute.timestamp != -1.0 && attribute.value != null) {
                    attributeSet(id, attribute)
                } else {
                    log.error("The Attribute $id has be ${id.action} with a timestamp of -1 0r value of null")
                }
            } else {
                log.error("Unrecognized message format in ${id.action} message from $id")
            }
        } catch (e: Exception) {
            log.error("Exception during ${id.action} message handling:", e)
        }
    }

    private fun handleAttributeDidSetMessage(id: ModelIdentifier, message: Message) {
        try {
            val data = message.body
            val messageFormat = serializationFormats.detect(data)
            if (messageFormat != null) {
                val attribute = messageFormat.deserializeAttributeSetStatus(data)
                if (attribute.timestamp != -1.0 && attribute.value != null) {
                    attributeDidSet(id, attribute)
                } else {
                    log.error("The Attribute $id has be ${id.action} with a timestamp of -1 0r value of null")
                }
            } else {
                log.error("Unrecognized message format in ${id.action} message from $id")
            }
        } catch (e: Exception) {
            log.error("Exception during ${id.action} message handling:", e)
        }
    }

    // Abstract method to handle update event of attribute.
    abstract fun attributeUpdated(id: ModelIdentifier, attribute: Attribute)

    // Abstract method to handle set event of attribute.
    abstract fun attributeSet(id: ModelIdentifier, attribute: AttributeSetCommand)

    // Abstract method to handle didSet event of attribute.
    abstract fun attributeDidSet(id: ModelIdentifier, attribute: AttributeSetStatus)
}
