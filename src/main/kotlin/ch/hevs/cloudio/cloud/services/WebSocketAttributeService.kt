package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractAttributeService
import ch.hevs.cloudio.cloud.model.Attribute
import org.springframework.context.ApplicationListener
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Controller
import org.springframework.messaging.simp.stomp.StompCommand

import org.springframework.messaging.simp.stomp.StompHeaderAccessor

import org.springframework.messaging.MessageChannel
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.Payload


@Controller
class WebSocketAttributeService(private val template: SimpMessagingTemplate) : AbstractAttributeService(){

    fun sendToClients(topic: String, attribute: Attribute) {
        template.convertAndSend(topic, attribute)
    }

    override fun attributeUpdated(attributeId: String, attribute: Attribute) {
        val topic = attributeId.replace(".", "/")
        sendToClients("/update/".plus(topic), attribute)
    }

    override fun attributeSet(attributeId: String, attribute: Attribute) {
        val topic = attributeId.replace(".", "/")
        sendToClients("/set/".plus(topic), attribute)
    }

    override fun attributeDidSet(attributeId: String, attribute: Attribute) {
        val topic = attributeId.replace(".", "/")
        sendToClients("/didSet/".plus(topic), attribute)
    }
}