package ch.hevs.cloudio.cloud.restapi.admin.usergroup

data class PostUserGroupEntity(
        val name: String,
        val metaData: Map<String, Any> = emptyMap()
)