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
    String;

    /**
     * Checks if the passed value object conforms to the type.
     *
     * @param value Value to check for type compatibility.
     * @return      True if the type matches, false otherwise.
     */
    fun checkType(value: Any?) = when {
        this == Boolean && value is kotlin.Boolean -> true
        this == Integer && (value is Int || value is Long) -> true
        this == Number && (value is Float || value  is Double) -> true
        this == String && (value is kotlin.String) -> true
        else -> false
    }
}
