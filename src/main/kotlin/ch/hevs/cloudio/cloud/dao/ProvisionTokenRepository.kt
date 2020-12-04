package ch.hevs.cloudio.cloud.dao

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProvisionTokenRepository : CrudRepository<ProvisionToken, Long> {
    fun findByToken(token: String): Optional<ProvisionToken>
    fun deleteByToken(token: String)
    fun deleteByEndpointUUID(uuid: UUID)
}
