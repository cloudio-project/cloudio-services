package ch.hevs.cloudio.cloud.security

/**
 * Defines the permission levels a user or group can have on an endpoint.
 */
enum class EndpointPermission(private val value: Int) {
    /**
     * Access to the endpoint is denied for the user or the group. This is the default permission if no other permission is available.
     */
    DENY(0),

    /**
     * The user or group can access part of the endpoint and only the part of the endpoint's data model that is accessible is visible to the user.
     */
    ACCESS(1),

    /**
     * The user or group can access part of the endpoint but the complete data model of the endpoint can be discovered, even the parts the user
     * does not have access to.
     *
     * *ACCESS* + browsing of data model.
     */
    BROWSE(2),

    /**
     * The user or group can read from the endpoint's data model. This includes the history for all data model elements.
     *
     * *BROWSE* + read all attribute's values.
     *
     */
    READ(3),

    /**
     * The user or group can read and modify from/to the endpoint's data model. This includes the history for all data model elements.
     *
     * *READ* + write to attributes.
     */
    WRITE(4),

    /**
     * The user or group can read and write from/to all attributes of the endpoint and additionally modify the endpoint's settings.
     *
     * *WRITE* + endpoint settings access.
     */
    CONFIGURE(5),

    /**
     * The user or the group can manage the endpoints settings, read and write from/to all attributes and additionally grant access to other users or group.
     *
     * *CONFIGURE* + granting access to endpoint for other users/groups.
     */
    GRANT(6),

    /**
     * The user owns the endpoint. Only one user can own the endpoint at a given time. Only the user that owns the endpoint is able to delete it.
     *
     * *GRANT* + delete endpoint.
     */
    OWN(7);

    /**
     * Returns true if the permission fulfills the passed requirement.
     *
     * @param requirement   The required permission level.
     * @return              True if the permission fulfills the required permission, false if not.
     */
    fun fulfills(requirement: EndpointPermission) = value >= requirement.value

    /**
     * Returns the higher permission: Either the actual permission or the permission passed as parameter.
     *
     * @param permission    Permission to check against.
     * @return              Higher permission.
     */
    fun higher(permission: EndpointPermission) = arrayOf(this, permission).maxByOrNull { it.value } ?: DEFAULT

    /**
     * Returns the lower permission: Either the actual permission or the permission passed as parameter.
     *
     * @param permission    Permission to check against.
     * @return              Lower permission.
     */
    fun lower(permission: EndpointPermission) = arrayOf(this, permission).minByOrNull { it.value } ?: DEFAULT

    companion object {
        val DEFAULT = DENY
    }
}
