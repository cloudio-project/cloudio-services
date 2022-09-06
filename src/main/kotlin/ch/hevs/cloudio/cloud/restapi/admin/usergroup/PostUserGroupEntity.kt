package ch.hevs.cloudio.cloud.restapi.admin.usergroup

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserGroupCreationOptions")
data class PostUserGroupEntity(
        @Schema(description = "Name for the new user group.", writeOnly = true, example = "IT Department")
        val name: String,

        @Schema(description = "Metadata to store for the new user group.", writeOnly = true, required = false,  defaultValue = "{}", example = "{\"location\": \"Sion\"}")
        val metaData: Map<String, Any>? = null
)