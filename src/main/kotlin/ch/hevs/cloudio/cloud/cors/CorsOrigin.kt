package ch.hevs.cloudio.cloud.cors

import ch.hevs.cloudio.cloud.dao.BaseEntity
import javax.persistence.*

@Entity
@Table(name = "cloudio_cors_allowed_origins")
data class CorsOrigin(
        @Column(length = 1024, nullable = false, unique = true)
        val origin: String = ""
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}