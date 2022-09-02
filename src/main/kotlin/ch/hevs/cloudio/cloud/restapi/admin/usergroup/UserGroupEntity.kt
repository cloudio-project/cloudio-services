package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserGroup")
data class UserGroupEntity(
        val name: String,
        val metaData: Map<String, Any>
)