package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLogService
import ch.hevs.cloudio.cloud.model.LogMessage
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class WebSocketLogService(private val template: SimpMessagingTemplate): AbstractLogService() {

    fun sendToClients(endpointUUID: UUID, message: LogMessage) {
        template.convertAndSend("/log/".plus(endpointUUID.toString()), message)
    }

    override fun logMessage(endpointUUID: UUID, message: LogMessage) {
        sendToClients(endpointUUID, message)
    }
}