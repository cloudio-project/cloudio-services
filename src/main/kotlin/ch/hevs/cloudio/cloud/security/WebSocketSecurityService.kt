package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class WebSocketSecurityService(private val permissionManager: CloudioPermissionManager): ChannelInterceptor{

    @Transactional
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        if(message.headers["stompCommand"] == StompCommand.SUBSCRIBE){
            val simpUser = message.headers["simpUser"]
            if (simpUser is UsernamePasswordAuthenticationToken){
                val userDetails = simpUser.principal
                if(userDetails is CloudioUserDetails){
                    var topic = message.headers["simpDestination"]
                    if(topic is String){
                        topic = topic.removePrefix("/topic/")
                        val modelIdentifier = ModelIdentifier(topic)

                        // Resolve the access level the user has to the element.
                        if (permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ)) {
                            return super.preSend(message, channel)
                        }
                    }
                }
            }
        }
        else{
            return super.preSend(message, channel)
        }
        return null
    }
}