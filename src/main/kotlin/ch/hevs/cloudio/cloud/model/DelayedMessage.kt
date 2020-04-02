package ch.hevs.cloudio.cloud.model

data class DelayedMessage(
        var topic: String,
        var data: Any
)