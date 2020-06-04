package ch.hevs.cloudio.cloud.restapi.admin.usergroup

data class UserGroupEntity(
        val name: String,
        val metaData: Map<String, Any>
)