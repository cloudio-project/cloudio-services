package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserEndpointPermissionRepository : CrudRepository<UserEndpointPermission, Long> {
    fun existsByUserIDAndEndpointUUID(userID: Long, endpointUUID: UUID): Boolean

    fun findByUserID(userID: Long): Collection<UserEndpointPermission>
    fun findByEndpointUUID(endpointUUID: UUID): Collection<UserEndpointPermission>
    fun findByUserIDAndEndpointUUID(userID: Long, endpointUUID: UUID): Optional<UserEndpointPermission>

    fun deleteByEndpointUUID(endpointUUID: UUID)
    fun deleteByUserIDAndEndpointUUID(userID: Long, endpointUUID: UUID)
}
