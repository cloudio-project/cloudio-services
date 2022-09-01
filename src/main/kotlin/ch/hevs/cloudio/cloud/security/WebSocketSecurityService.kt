package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.apache.commons.logging.LogFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class WebSocketSecurityService(private val permissionManager: CloudioPermissionManager): ChannelInterceptor{
    private val log = LogFactory.getLog(WebSocketSecurityService::class.java)

    @Transactional
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        if(message.headers["stompCommand"] == StompCommand.SUBSCRIBE){
            val simpUser = message.headers["simpUser"]
            if (simpUser is UsernamePasswordAuthenticationToken){
                val userDetails = simpUser.principal
                if(userDetails is CloudioUserDetails){
                    var topic = message.headers["simpDestination"]
                    if(topic is String){
                        if(topic.startsWith("/log/")){
                            topic = topic.removePrefix("/log/")
                            val uuid = ModelIdentifier(topic).endpoint

                            // Resolve the access leve the user has to the endpoint
                            if(permissionManager.hasEndpointPermission(userDetails, uuid, EndpointPermission.READ)){
                                return super.preSend(message, channel)
                            }
                            else{
                                log.info("Subscribe to $uuid logs denied for user ${userDetails.username}")
                                return null
                            }
                        }
                        // if action is update set or didSet
                        else{
                            //remove action
                            topic = topic.drop(1).dropWhile { it != '/' }.drop(1)

                            val modelIdentifier = ModelIdentifier(topic)

                            // Resolve the access level the user has to the element.
                            if (permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ)) {
                                return super.preSend(message, channel)
                            }
                            else{
                                log.info("Subscribe to $topic denied for user ${userDetails.username}")
                                return null
                            }
                        }
                    }
                }
            }
            log.info("Wrong STOMP subscribe message format")
        }
        else{
            return super.preSend(message, channel)
        }
        return null
    }
}