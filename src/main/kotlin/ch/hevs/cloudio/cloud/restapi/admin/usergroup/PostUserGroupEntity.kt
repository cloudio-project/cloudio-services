package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserGroupCreationOptions")
data class PostUserGroupEntity(
        val name: String,
        val metaData: Map<String, Any> = emptyMap()
)