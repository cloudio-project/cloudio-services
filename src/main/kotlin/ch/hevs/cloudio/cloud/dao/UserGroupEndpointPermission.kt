package ch.hevs.cloudio.cloud.dao

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
        val endpointUUID: UUID = UUID(0, 0),

        permission: EndpointPermission = EndpointPermission.DEFAULT,

        @ElementCollection(fetch = FetchType.EAGER)
        @JoinTable(name = "cloudio_user_group_endpoint_model_element_permission")
        @Column(name = "permission", nullable = false)
        @MapKeyColumn(name = "model_identifier")
        val modelPermissions: MutableMap<String, EndpointModelElementPermission> = mutableMapOf()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Enumerated(EnumType.STRING)
    var permission: EndpointPermission = permission.lower(EndpointPermission.GRANT)
        set(value) {
            field = value.lower(EndpointPermission.GRANT)
        }
}
