package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserEndpointGroupPermissionRepository : CrudRepository<UserEndpointGroupPermission, Long> {
    fun existsByUserIDAndEndpointGroupID(userID: Long, endpointGroupID: Long): Boolean

    fun findByUserID(userID: Long): Collection<UserEndpointGroupPermission>
    fun findByEndpointGroupID(endpointGroupID: Long): Collection<UserEndpointGroupPermission>
    fun findByUserIDAndEndpointGroupID(userID: Long, endpointGroupID: Long): Optional<UserEndpointGroupPermission>

    fun deleteByEndpointGroupID(endpointGroupID: Long)
    @Transactional
    fun deleteByUserIDAndEndpointGroupID(userID: Long, endpointGroupID: Long)
}