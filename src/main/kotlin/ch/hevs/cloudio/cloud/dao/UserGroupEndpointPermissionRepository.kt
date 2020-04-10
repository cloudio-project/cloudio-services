package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupEndpointPermissionRepository : CrudRepository<UserGroupEndpointPermission, Long> {
    fun findByUserGroupID(id: Long)
    fun findByEndpointUUID(uuid: UUID)
}
