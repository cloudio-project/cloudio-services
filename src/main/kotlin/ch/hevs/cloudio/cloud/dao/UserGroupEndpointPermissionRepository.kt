package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserGroupEndpointPermissionRepository : CrudRepository<UserGroupEndpointPermission, Long> {
    fun existsByUserGroupIDAndEndpointUUID(userGroupID: Long, endpointUUID: UUID): Boolean

    fun findByUserGroupID(userGroupID: Long)
    fun findByEndpointUUID(endpointUUID: UUID)
    fun findByUserGroupIDAndEndpointUUID(userGroupID: Long, endpointUUID: UUID): Optional<UserGroupEndpointPermission>

    fun deleteByUserGroupIDAndEndpointUUID(userGroupID: Long, endpointUUID: UUID)
}
