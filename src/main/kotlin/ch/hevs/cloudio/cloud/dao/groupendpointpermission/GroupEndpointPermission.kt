package ch.hevs.cloudio.cloud.dao.groupendpointpermission

import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Entity
@IdClass(GroupEndpointPermission.Key::class)
data class GroupEndpointPermission(
        @Id var groupName: String = "",
        @Id var endpoint: UUID = UUID(0, 0),
        var permission: EndpointPermission = EndpointPermission.DEFAULT
) {
    data class Key(
            private var groupName: String = "",
            private var endpoint: UUID = UUID(0, 0)
    ) : Serializable
}
