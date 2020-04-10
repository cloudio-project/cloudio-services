package ch.hevs.cloudio.cloud.repo.authentication

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MONGOUserRepository : MongoRepository<User, String> {

    @Query("{ 'userGroups': ?0 }")
    fun findByGroupMembership(groupName: String): List<User>

}
