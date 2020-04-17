package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_user_group_endpoint_permission", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_group_id", "endpoint_uuid"])
])
data class UserGroupEndpointPermission(
        @Column(name = "user_group_id")
        val userGroupID: Long = 0,

        @Column(name = "endpoint_uuid", nullable = false)
        val endpointUUID: UUID = UUID(0, 0),

        @Enumerated(EnumType.STRING)
        var permission: EndpointPermission = EndpointPermission.DEFAULT
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
