package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserGroupEndpointPermissionRepository : CrudRepository<UserGroupEndpointPermission, Long> {
    fun existsByUserGroupIDAndEndpointUUID(userGroupID: Long, endpointUUID: UUID): Boolean

    fun findByUserGroupID(userGroupID: Long): Collection<UserGroupEndpointPermission>
    fun findByUserGroupIDIn(userGroupIDs: Collection<Long>): Collection<UserGroupEndpointPermission>
    fun findByEndpointUUID(endpointUUID: UUID): Collection<UserGroupEndpointPermission>
    fun findByUserGroupIDAndEndpointUUID(userGroupID: Long, endpointUUID: UUID): Optional<UserGroupEndpointPermission>

    fun deleteByEndpointUUID(endpointUUID: UUID)
    @Transactional
    fun deleteByUserGroupIDAndEndpointUUID(userGroupID: Long, endpointUUID: UUID)
}
