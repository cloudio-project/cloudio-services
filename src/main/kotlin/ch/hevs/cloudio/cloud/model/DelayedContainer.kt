package ch.hevs.cloudio.cloud.model

data class DelayedContainer (
    var timestamp: Double = -1.0,
    var messages: Set<DelayedMessage> = mutableSetOf()
)
