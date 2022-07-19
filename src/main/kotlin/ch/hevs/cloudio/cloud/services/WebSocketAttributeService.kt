package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractAttributeService
import ch.hevs.cloudio.cloud.model.Attribute
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class WebSocketAttributeService(private val template: SimpMessagingTemplate) : AbstractAttributeService()  {


    fun sendToClients(attributeId: String, attribute: Attribute) {
        val topic = attributeId.replace(".", "/")
        template.convertAndSend("/topic/".plus(topic), attribute)
    }

    override fun attributeUpdated(attributeId: String, attribute: Attribute) {
        sendToClients(attributeId, attribute)
    }

    override fun attributeSet(attributeId: String, attribute: Attribute) {
        sendToClients(attributeId, attribute)
    }
}