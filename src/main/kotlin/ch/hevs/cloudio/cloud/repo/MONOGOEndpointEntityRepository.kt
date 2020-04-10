package ch.hevs.cloudio.cloud.repo

import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface MONOGOEndpointEntityRepository : MongoRepository<EndpointEntity, UUID>
