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
     * The user or group can see the model element and all it's children if not another permission is attributed to one of the children. Note that this does
     * not include to read the actual value.
     */
    VIEW(1),

    /**
     * The user or group can read the model element and all it's children if not another permission is attributed to one of the children.
     */
    READ(2),

    /**
     * The user or group can read and write the model element and all it's children if not another permission is attributed to one of the children that
     * overrides this one.
     */
    WRITE(3);

    /**
     * Returns true if the permission fulfills the passed requirement.
     *
     * @param requirement   The required permission level.
     * @return              True if the permission fulfills the required permission, false if not.
     */
    fun fulfills(requirement: EndpointModelElementPermission) = value >= requirement.value

    /**
     * Returns the higher permission: Either the actual permission or the permission passed as parameter.
     *
     * @param permission    Permission to check against.
     * @return              Higher permission.
     */
    fun higher(permission: EndpointModelElementPermission) = arrayOf(this, permission).maxByOrNull { it.value }!!

    /**
     * Returns the lower permission: Either the actual permission or the permission passed as parameter.
     *
     * @param permission    Permission to check against.
     * @return              Lower permission.
     */
    fun lower(permission: EndpointModelElementPermission) = arrayOf(this, permission).minByOrNull { it.value }!!

    companion object {
        val DEFAULT = DENY
    }
}
