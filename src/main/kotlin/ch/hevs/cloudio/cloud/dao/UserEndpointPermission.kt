package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import ch.hevs.cloudio.cloud.security.AbstractEndpointPermission
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
        override val endpointUUID: UUID = UUID(0, 0),

        @Enumerated(EnumType.STRING)
        @Column(length = 32)
        override var permission: EndpointPermission = EndpointPermission.DEFAULT,

        @ElementCollection(fetch = FetchType.LAZY)
        @JoinTable(name = "cloudio_user_endpoint_model_element_permission")
        @Column(name = "permission", nullable = false, length = 32)
        @MapKeyColumn(name="model_path")
        @Enumerated(EnumType.STRING)
        override val modelPermissions: MutableMap<String, EndpointModelElementPermission> = mutableMapOf()
) : AbstractEndpointPermission() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
