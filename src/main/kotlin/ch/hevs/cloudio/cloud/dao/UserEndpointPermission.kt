package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_user_endpoint_permission", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "endpoint_uuid"])
])
data class UserEndpointPermission(
        @Column(name = "user_id")
        val userID: Long = 0,

        @Column(name = "endpoint_uuid", nullable = false)
        val endpointUUID: UUID = UUID(0, 0),

        @Enumerated(EnumType.STRING)
        var permission: EndpointPermission = EndpointPermission.DEFAULT,

        @ElementCollection(fetch = FetchType.EAGER)
        @JoinTable(name = "cloudio_user_endpoint_model_element_permission")
        @Column(name = "permission", nullable = false)
        @MapKeyColumn(name="model_identifier")
        val modelPermissions: MutableMap<String, EndpointModelElementPermission> = mutableMapOf()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
