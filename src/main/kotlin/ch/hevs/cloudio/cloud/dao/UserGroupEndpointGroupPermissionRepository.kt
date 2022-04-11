package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserGroupEndpointGroupPermissionRepository : CrudRepository<UserGroupEndpointGroupPermission, Long> {
    fun existsByUserGroupIDAndEndpointGroupID(userGroupID: Long, endpointGroupID: Long): Boolean

    fun findByUserGroupID(userGroupID: Long): Collection<UserGroupEndpointGroupPermission>
    fun findByEndpointGroupID(endpointGroupID: Long): Collection<UserGroupEndpointGroupPermission>
    fun findByUserGroupIDIn(userGroupIDs: Collection<Long>): Collection<UserGroupEndpointGroupPermission>
    fun findByUserGroupIDAndEndpointGroupID(userGroupID: Long, endpointGroupID: Long): Optional<UserGroupEndpointGroupPermission>

    fun deleteByEndpointGroupID(endpointGroupID: Long)
    @Transactional
    fun deleteByUserGroupIDAndEndpointGroupID(userGroupID: Long, endpointGroupID: Long)
}