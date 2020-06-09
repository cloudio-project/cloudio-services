package ch.hevs.cloudio.cloud.dao

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_provision_token")
data class ProvisionToken(
        @Column(unique = true, nullable = false, updatable = false, length = 255)
        val token: String = generateToken(),

        @Column(nullable = false, updatable = false)
        val endpointUUID: UUID = UUID(0, 0),

        @Column(nullable = false, updatable = false)
        val expires: Date = Date()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        private fun generateToken(): String {
            val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return List(255) { alphabet.random() }.joinToString("")
        }
    }
}
