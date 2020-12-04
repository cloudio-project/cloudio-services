package ch.hevs.cloudio.cloud.model

/**
 * Defines the different possible attribute constraints.
 * The constraint basically defines which actions are possible on a given attribute.
 * @see Attribute
 */
enum class AttributeConstraint {
    /**
     * Invalid constraint.
     */
    Invalid,

    /**
     * The attribute is a static value and can't be changed during runtime. It can only be part of the @online message.
     */
    Static,

    /**
     * The attribute is a parameter that can be configured from the cloud and its value should be saved persistent by the endpoint.
     */
    Parameter,

    /**
     * The attribute is a status. In opposite to the [AttributeConstraint::Measure], a status is a value **calculated** by the endpoint.
     */
    Status,

    /**
     * The attribute is a set-point that can be changed from the cloud. Set-points are never stored persistently by an endpoint and will be initialized to their default value on the endpoint's
     * next power cycle or reset.
     */
    SetPoint,

    /**
     * The attribute is a physical measure.
     */
    Measure
}
