package ch.hevs.cloudio.cloud.cors

import org.springframework.data.repository.CrudRepository

interface CorsRepository : CrudRepository<CorsOrigin, Long>{
    fun existsByOrigin(originName: String): Boolean
    fun deleteByOrigin(originName: String)
}