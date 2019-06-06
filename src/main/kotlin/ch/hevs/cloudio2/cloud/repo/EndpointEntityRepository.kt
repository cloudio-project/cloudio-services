package ch.hevs.cloudio2.cloud.repo

import org.springframework.data.mongodb.repository.MongoRepository

interface EndpointEntityRepository: MongoRepository<EndpointEntity, String>