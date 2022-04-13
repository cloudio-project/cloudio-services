package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import java.util.*

interface EndpointGroupRepository : CrudRepository<EndpointGroup, Long> {
    fun existsByGroupName(groupName: String): Boolean
    fun findByGroupName(groupName: String): Optional<EndpointGroup>
    fun deleteByGroupName(groupName: String)
}