package ch.hevs.cloudio.cloud.model

/**
 * Represents a late message.
 * @see DelayedMessages
 */
data class DelayedMessage(
        var topic: String,
        var data: Any
)
