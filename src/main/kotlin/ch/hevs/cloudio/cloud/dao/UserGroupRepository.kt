package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserGroupRepository : CrudRepository<UserGroup, Long> {
    fun findByGroupName(groupName: String): Optional<UserGroup>

    fun deleteByGroupName(groupName: String)
}
