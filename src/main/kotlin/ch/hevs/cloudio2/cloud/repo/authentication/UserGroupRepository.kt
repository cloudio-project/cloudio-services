package ch.hevs.cloudio2.cloud.repo.authentication

import org.springframework.data.mongodb.repository.MongoRepository

interface UserGroupRepository : MongoRepository<UserGroup, String>{
}