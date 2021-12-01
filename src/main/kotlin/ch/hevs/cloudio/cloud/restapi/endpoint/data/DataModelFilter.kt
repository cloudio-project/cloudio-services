package ch.hevs.cloudio.cloud.restapi.endpoint.data

import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import org.springframework.security.core.Authentication

abstract class DataModelFilter {

    companion object {
        fun filterEndpoint(endpoint: EndpointDataModel, permissionManager: CloudioPermissionManager,
                           authentication: Authentication, modelIdentifier: ModelIdentifier): EndpointDataModel? {
            val e = EndpointDataModel()
            e.messageFormatVersion = endpoint.messageFormatVersion
            e.supportedFormats = endpoint.supportedFormats
            e.version = endpoint.version

            endpoint.nodes.forEach {
                val nodeId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterNode(it.value, permissionManager, authentication, nodeId)
                if (temp is Node) {
                    e.nodes[it.key] = temp
                }
            }

            if (e.nodes.isNotEmpty()) {
                return e
            }

            return null
        }


        fun filterNode(node: Node, permissionManager: CloudioPermissionManager,
                       authentication: Authentication, modelIdentifier: ModelIdentifier): Node? {

            if (permissionManager.hasEndpointModelElementPermission(authentication.userDetails(),
                            modelIdentifier, EndpointModelElementPermission.READ)) {
                return node
            }
            var n = Node()
            n.online = n.online
            n.implements = n.implements

            node.objects.forEach {
                val objId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterObject(it.value, permissionManager, authentication, objId)
                if (temp is CloudioObject) {
                    n.objects[it.key] = temp
                }
            }

            if (n.objects.isNotEmpty()) {
                return n
            }

            return null
        }

        fun filterObject(obj: CloudioObject, permissionManager: CloudioPermissionManager,
                         authentication: Authentication, modelIdentifier: ModelIdentifier): CloudioObject? {

            if (permissionManager.hasEndpointModelElementPermission(authentication.userDetails(),
                            modelIdentifier, EndpointModelElementPermission.READ)) {
                return obj
            }

            var o = CloudioObject()
            o.conforms = obj.conforms

            obj.objects.forEach {
                val objId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterObject(it.value, permissionManager, authentication, objId)
                if (temp is CloudioObject) {
                    o.objects[it.key] = temp
                }
            }

            obj.attributes.forEach {
                val attrId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterAttribute(it.value, permissionManager, authentication, attrId)
                if (temp is Attribute) {
                    o.attributes[it.key] = temp
                }
            }

            if (o.objects.isNotEmpty() || o.attributes.isNotEmpty()) {
                return o
            }

            return null
        }

        fun filterAttribute(attribute: Attribute, permissionManager: CloudioPermissionManager,
                            authentication: Authentication, modelIdentifier: ModelIdentifier): Attribute? {

            if (permissionManager.hasEndpointModelElementPermission(authentication.userDetails(),
                            modelIdentifier, EndpointModelElementPermission.READ)) {
                return attribute
            }

            return null
        }
    }
}