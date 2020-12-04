package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import java.util.*

interface EndpointRepository : CrudRepository<Endpoint, UUID>