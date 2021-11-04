package ch.hevs.cloudio.cloud.abstractservices

import ch.hevs.cloudio.cloud.abstractservices.messaging.AbstractTopicService
import ch.hevs.cloudio.cloud.model.ActionIdentifier
import ch.hevs.cloudio.cloud.model.LogLevel
import ch.hevs.cloudio.cloud.model.LogMessage
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import ch.hevs.cloudio.cloud.serialization.detect
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

abstract class AbstractLogService(ignoresLogMessages: Boolean = false, ignoresLogLevel: Boolean = false) :
    AbstractTopicService(mutableSetOf<String>().apply {
        if (!ignoresLogMessages) add("${ActionIdentifier.LOG_OUTPUT}.#")
        if (!ignoresLogLevel) add("${ActionIdentifier.LOG_LEVEL}.#")
    }.toSet()) {

    private val log = LogFactory.getLog(AbstractLogService::class.java)

    @Autowired
    private lateinit var serializationFormats: Collection<SerializationFormat>

    final override fun handleMessage(message: Message) {
        val id = ModelIdentifier(message.messageProperties.receivedRoutingKey)
        if (id.valid && id.count() == 0) {
            when (id.action) {
                ActionIdentifier.LOG_OUTPUT -> handleLogOutputMessage(id.endpoint, message)
                ActionIdentifier.LOG_LEVEL -> handleLogLevelMessage(id.endpoint, message)
                else -> log.error("Unexpected action: ${id.action}")
            }
        } else {
            log.error("Invalid topic: ${message.messageProperties.receivedRoutingKey}")
        }
    }

    private fun handleLogOutputMessage(endpointUUID: UUID, message: Message) {
        val data = message.body
        val messageFormat = serializationFormats.detect(data)
        if (messageFormat != null) {
            val logMessage = messageFormat.deserializeLogMessage(data)
            if (logMessage.timestamp != -1.0) {
                logMessage(endpointUUID, logMessage)
            } else {
                log.error("Received log message without timestamp from $endpointUUID")
            }
        } else {
            log.error("Unrecognized message format in @logs message from $endpointUUID")
        }
    }

    private fun handleLogLevelMessage(endpointUUID: UUID, message: Message) {
        val data = message.body
        val messageFormat = serializationFormats.detect(data)
        if (messageFormat != null) {
            logLevelChanged(endpointUUID, messageFormat.deserializeLogLevel(data))
        } else {
            log.error("Unrecognized message format in @logsLevel message from $endpointUUID")
        }
    }

    // Abstract method to handle logs messages.
    open fun logMessage(endpointUUID: UUID, message: LogMessage) {}

    // Abstract method to handle log level change messages.
    open fun logLevelChanged(endpointUUID: UUID, level: LogLevel) {}
}
