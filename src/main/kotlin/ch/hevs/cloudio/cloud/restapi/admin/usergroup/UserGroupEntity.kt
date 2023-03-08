package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import ch.hevs.cloudio.cloud.restapi.admin.user.ListUserEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserGroup")
data class UserGroupEntity(
        @Schema(description = "User group name.", readOnly = true, example = "IT Department")
        var name: String,

        @Schema(description = "User group meta data.", example = "{\"location\": \"Sion\"}")
        val metaData: Map<String, Any>,

        @Schema(description = "Users belonging to the group.", readOnly = true)
        val users: List<ListUserEntity>
)