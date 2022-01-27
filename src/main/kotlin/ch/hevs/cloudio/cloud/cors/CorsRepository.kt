package ch.hevs.cloudio.cloud.cors

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CorsRepository : CrudRepository<CorsOrigin, Long>{
    fun existsByOrigin(originName: String): Boolean
    fun deleteByOrigin(originName: String)
}