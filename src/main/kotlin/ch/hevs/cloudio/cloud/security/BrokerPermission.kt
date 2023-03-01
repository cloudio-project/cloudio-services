package ch.hevs.cloudio.cloud.security

enum class BrokerPermission(val value: Int) {
    DENY(0),
    READ(1),
    WRITE(2),
    CONFIGURE(3);

    fun toEndpointPermission(): EndpointPermission = when(this) {
        DENY -> EndpointPermission.DENY
        READ -> EndpointPermission.READ
        else -> EndpointPermission.WRITE
    }

    fun toEndpointModelElementPermission(): EndpointModelElementPermission = when(this) {
        DENY -> EndpointModelElementPermission.DENY
        READ -> EndpointModelElementPermission.READ
        else -> EndpointModelElementPermission.WRITE
    }
}
