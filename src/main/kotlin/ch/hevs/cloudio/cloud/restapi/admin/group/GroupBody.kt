package ch.hevs.cloudio.cloud.restapi.admin.group

import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroup

data class GroupBody(
        val name: String,
        var permissions: Map<String, PrioritizedPermission> = emptyMap()
) {
    constructor(userGroup: UserGroup) : this(
            name = userGroup.userGroupName,
            permissions = userGroup.permissions
    )

    fun updateUserGroup(userGroup: UserGroup) {
        userGroup.permissions = permissions
    }
}
