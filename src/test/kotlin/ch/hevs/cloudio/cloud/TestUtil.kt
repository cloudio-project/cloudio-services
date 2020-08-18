package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.restapi.admin.usergroup.UserGroupEntity
import ch.hevs.cloudio.cloud.security.BrokerPermission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission

object TestUtil {

    private val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generateRandomString(length: Int): String {
        return List(length) { alphabet.random() }.joinToString("")
    }
}