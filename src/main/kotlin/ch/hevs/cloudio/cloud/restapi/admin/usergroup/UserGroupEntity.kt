package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import ch.hevs.cloudio.cloud.dao.UserGroup
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserGroup")
data class UserGroupEntity(
        @Schema(description = "User group name.", readOnly = true, example = "IT Department")
        var name: String,

        @Schema(description = "User group meta data.", example = "{\"location\": \"Sion\"}")
        val metaData: Map<String, Any>
) {
        constructor(userGroup: UserGroup) : this(userGroup.groupName, userGroup.metaData)
}