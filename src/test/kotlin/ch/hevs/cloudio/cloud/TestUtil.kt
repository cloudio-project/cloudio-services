package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import ch.hevs.cloudio.cloud.restapi.admin.group.GroupEntity
import ch.hevs.cloudio.cloud.restapi.admin.user.PostUserEntity
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import java.util.*

object TestUtil {

    private val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generateRandomString(length: Int): String {
        return List(length) { alphabet.random() }.joinToString("")
    }

    fun createUser(): PostUserEntity {

        return PostUserEntity(
                password = "123456",
                permissions = mapOf("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/inside/temperature" to PrioritizedPermission(Permission.DENY, PermissionPriority.HIGH),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/*/temperatures/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/error/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/*/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW)),
                groupMemberships = setOf("testGroup1", "testGroup2"),
                authorities = setOf())
    }

    fun createUserGroup(userGroupName: String): GroupEntity {

        return GroupEntity(userGroupName,
                mutableMapOf("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/inside/temperature" to PrioritizedPermission(Permission.DENY, PermissionPriority.HIGH),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/*/temperatures/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/error/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW)))

    }

    fun createEndpointEntity(endpointUuid: String, friendlyName: String): EndpointEntity {
        return EndpointEntity(endpointUuid = UUID.fromString(endpointUuid), friendlyName = friendlyName, endpoint =
        Endpoint("v0.2",
                setOf("JSON"),
                hashMapOf("demoNode" to
                Node(emptySet(), hashMapOf("demoObject" to
                        CloudioObject("", mutableMapOf(), hashMapOf(
                                "demoMeasure" to Attribute(AttributeConstraint.Measure, AttributeType.Number, 10.0, 10.0),
                                "demoSetPoint" to Attribute(AttributeConstraint.SetPoint, AttributeType.Number, 10.0, 10.0)
                        )))))))
    }
}