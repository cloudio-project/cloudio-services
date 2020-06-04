package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Long> {
    fun existsByUserName(userName: String): Boolean
    fun findByUserName(userName: String): Optional<User>
    fun deleteByUserName(userName: String)

    fun findByGroupMembershipsContains(userGroup: UserGroup): Collection<User>
}
