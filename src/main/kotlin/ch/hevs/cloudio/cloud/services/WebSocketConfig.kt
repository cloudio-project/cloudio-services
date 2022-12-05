package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.cors.CorsRepository
import ch.hevs.cloudio.cloud.security.WebSocketSecurityService
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.*


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
        private val webSocketSecurityService: WebSocketSecurityService,
        private val corsRepository: CorsRepository
        ) : WebSocketMessageBrokerConfigurer {

    private var eventEndpoint : StompWebSocketEndpointRegistration? = null

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/update", "/set", "/didSet", "/log")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        eventEndpoint = registry.addEndpoint("/api/v1/events")

        updateAllowedOrigins()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketSecurityService)
    }

    fun updateAllowedOrigins(){
        val origins = mutableListOf<String>()

        corsRepository.findAll().forEach {
            origins.add(it.origin)
        }

        val array : Array<String> = origins.toTypedArray()

        eventEndpoint?.setAllowedOrigins(*array)
    }
}