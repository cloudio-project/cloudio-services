package ch.hevs.cloudio.cloud.model

/**
 * Send by an endpoint once it has resumed the connection to the message broker to send messages that it was not able to send during the interruption with the action @delayed.
 * @see ActionIdentifier.DELAYED_MESSAGES
 */
data class DelayedMessages(
        /**
         * Timestamp when the delayed messages could be finally send by the endpoint.
         */
        var timestamp: Double = -1.0,

        /**
         * Late messages.
         */
        var messages: List<DelayedMessage> = mutableListOf()
)
