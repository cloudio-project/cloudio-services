package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

object TestUtil {

    private val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private var encoder: PasswordEncoder = BCryptPasswordEncoder()

    fun generateRandomString(length: Int): String {
        return List(length) { alphabet.random() }.joinToString("")
    }

    fun createUser(userName: String): User {

        return User(userName,
                encoder.encode("123456"),
                mapOf("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/inside/temperature" to PrioritizedPermission(Permission.DENY, PermissionPriority.HIGH),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/*/temperatures/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/error/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/*/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW)),
                setOf("testGroup1", "testGroup2"),
                setOf(Authority.BROKER_ADMINISTRATION, Authority.HTTP_ACCESS, Authority.HTTP_ADMIN))
    }

    fun createUserGroup(userGroupName: String, userNames: Set<String>): UserGroup {

        return UserGroup(userGroupName,
                userNames,
                mapOf("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/inside/temperature" to PrioritizedPermission(Permission.DENY, PermissionPriority.HIGH),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/*/temperatures/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/error/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                        "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW)))

    }
}