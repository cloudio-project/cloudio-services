package ch.hevs.cloudio.cloud.security

/**
 * Defines the permission levels a user or group can have on an model element of an endpoint.
 */
enum class EndpointModelElementPermission(private val value: Int) {
    /**
     * Access to the endpoint data model element is denied for the user or the group and all elements that are children of the element itself until another
     * permission is attributed to one of such elements. This is the default permission if no other permission is available.
     */
    DENY(0),

    /**
     * The user or group can read the model element and all it's children if not another permission is attributed to one of the children.
     */
    READ(1),

    /**
     * The user or group can read and write the model element and all it's children if not another permission is attributed to one of the children that
     * overrides this one.
     */
    WRITE(2);

    /**
     * Returns true if the permission fulfills the passed requirement.
     *
     * @param requirement   The required permission level.
     * @return              True if the permission fulfills the required permission, false if not.
     */
    fun fulfills(requirement: EndpointModelElementPermission) = value >= requirement.value

    companion object {
        val DEFAULT = DENY
    }
}
