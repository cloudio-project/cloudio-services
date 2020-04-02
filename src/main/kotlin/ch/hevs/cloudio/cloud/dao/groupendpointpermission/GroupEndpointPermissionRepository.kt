package ch.hevs.cloudio.cloud.dao.groupendpointpermission

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GroupEndpointPermissionRepository : CrudRepository<GroupEndpointPermission, GroupEndpointPermission.Key> {
}