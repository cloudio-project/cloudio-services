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




@Controller
class WebSocketAttributeService(private val template: SimpMessagingTemplate) : AbstractAttributeService(), ChannelInterceptor {

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

    @SubscribeMapping("/**")
    fun subscribe(topic: String){
        print(topic)
    }


    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        val command: StompCommand? = accessor.command
        if (StompCommand.SUBSCRIBE == command){
            print("alo")
        }
        return message
    }
}