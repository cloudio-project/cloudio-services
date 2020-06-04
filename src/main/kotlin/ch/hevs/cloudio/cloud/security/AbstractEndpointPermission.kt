package ch.hevs.cloudio.cloud.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties("id", "userID", "userGroupID")
open class AbstractEndpointPermission(
        open val endpointUUID: UUID = UUID(0 , 0),
        open var permission: EndpointPermission = EndpointPermission.DEFAULT,
        open val modelPermissions: MutableMap<String, EndpointModelElementPermission> = mutableMapOf()
)
