package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_user_endpoint_permission", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "endpoint_uuid"])
])
data class UserEndpointPermission(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @Column(name = "user_id")
        val userID: Long = 0,

        @Column(name = "endpoint_uuid", nullable = false)
        val endpointUUID: UUID = UUID(0, 0),

        @Enumerated(EnumType.STRING)
        var permission: EndpointPermission = EndpointPermission.DEFAULT
)
