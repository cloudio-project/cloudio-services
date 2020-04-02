package ch.hevs.cloudio.cloud.dao.userendpointpermission

import ch.hevs.cloudio.cloud.security.EndpointPermission
import java.io.Serializable
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Entity
@IdClass(UserEndpointPermission.Key::class)
data class UserEndpointPermission(
        @Id var userName: String = "",
        @Id var endpoint: UUID = UUID(0, 0),
        var permission: EndpointPermission = EndpointPermission.DEFAULT
) {
    data class Key(
            private var userName: String = "",
            private var endpoint: UUID = UUID(0, 0)
    ) : Serializable
}
