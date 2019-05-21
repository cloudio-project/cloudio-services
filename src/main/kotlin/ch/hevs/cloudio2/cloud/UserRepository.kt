package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.model.User
import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String>{
}