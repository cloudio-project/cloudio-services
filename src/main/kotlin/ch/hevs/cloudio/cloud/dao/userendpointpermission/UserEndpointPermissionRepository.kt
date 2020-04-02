package ch.hevs.cloudio.cloud.dao.userendpointpermission

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserEndpointPermissionRepository : CrudRepository<UserEndpointPermission, UserEndpointPermission.Key> {
}