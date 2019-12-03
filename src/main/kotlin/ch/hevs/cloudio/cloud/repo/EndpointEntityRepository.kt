package ch.hevs.cloudio.cloud.repo

import org.springframework.data.mongodb.repository.MongoRepository

interface EndpointEntityRepository : MongoRepository<EndpointEntity, String>