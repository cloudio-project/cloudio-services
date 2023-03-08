package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.AbstractEndpointPermission
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "cloudio_user_group_endpoint_permission", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_group_id", "endpoint_uuid"])
])
class UserGroupEndpointPermission(
        @Column(name = "user_group_id")
        val userGroupID: Long = 0,

        @Column(name = "endpoint_uuid", nullable = false)
        override val endpointUUID: UUID = UUID(0, 0),

        permission: EndpointPermission = EndpointPermission.DEFAULT,

        @ElementCollection(fetch = FetchType.LAZY)
        @JoinTable(name = "cloudio_user_group_endpoint_model_element_permission")
        @Column(name = "permission", nullable = false, length = 32)
        @MapKeyColumn(name = "model_path")
        @Enumerated(EnumType.STRING)
        override val modelPermissions: MutableMap<String, EndpointModelElementPermission> = mutableMapOf()
): AbstractEndpointPermission() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    override var permission: EndpointPermission = permission.lower(EndpointPermission.GRANT)
        set(value) {
            field = value.lower(EndpointPermission.GRANT)
        }
}
