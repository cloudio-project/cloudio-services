package ch.hevs.cloudio.cloud.repo.authentication

import org.springframework.data.mongodb.repository.MongoRepository

interface UserGroupRepository : MongoRepository<UserGroup, String>
