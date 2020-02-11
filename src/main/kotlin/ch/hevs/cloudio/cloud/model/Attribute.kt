package ch.hevs.cloudio.cloud.model

data class Attribute(
        var constraint: AttributeConstraint = AttributeConstraint.Invalid,
        var type: AttributeType = AttributeType.Invalid,
        var timestamp: Double = -1.0,
        var value: Any? = null
)
