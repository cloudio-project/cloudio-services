package ch.hevs.cloudio.cloud.model

/**
 * All data types a cloud.iO attribute can be.
 * @see Attribute
 */
enum class AttributeType {
    /**
     * Invalid data type.
     */
    Invalid,

    /**
     * The attribute is of type boolean.
     */
    Boolean,

    /**
     * The attribute is of type integer (Short, Integer or Long).
     */
    Integer,

    /**
     * The attribute is of type number (Float or Double).
     */
    Number,

    /**
     * The attribute is of type string.
     */
    String
}
