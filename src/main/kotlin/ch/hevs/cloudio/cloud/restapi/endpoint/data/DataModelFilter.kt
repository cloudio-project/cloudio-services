package ch.hevs.cloudio.cloud.restapi.endpoint.data

import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.model.*
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import ch.hevs.cloudio.cloud.security.EndpointModelElementPermission
import org.springframework.security.core.Authentication

abstract class DataModelFilter {

    companion object {

        /**
         * filter the given structure on the given EndpointModelElementPermission
         */
        fun filter(data: Any, permissionManager: CloudioPermissionManager,
                   authentication: Authentication, modelIdentifier: ModelIdentifier,
                   permission: EndpointModelElementPermission): Any? {

            when (data) {
                is EndpointDataModel -> {
                    return filterEndpoint(data, permissionManager, authentication,
                            modelIdentifier, permission)
                }
                is Node -> {
                    return filterNode(data, permissionManager, authentication,
                            modelIdentifier, permission)
                }
                is CloudioObject -> {
                    return filterObject(data, permissionManager, authentication,
                            modelIdentifier, permission)
                }
                is Attribute -> {
                    return filterAttribute(data, permissionManager, authentication,
                            modelIdentifier, permission)
                }
            }

            return null
        }

        fun filterEndpoint(endpoint: EndpointDataModel, permissionManager: CloudioPermissionManager,
                           authentication: Authentication, modelIdentifier: ModelIdentifier,
                           permission: EndpointModelElementPermission): EndpointDataModel? {
            val e = EndpointDataModel()
            e.messageFormatVersion = endpoint.messageFormatVersion
            e.supportedFormats = endpoint.supportedFormats
            e.version = endpoint.version

            endpoint.nodes.forEach {
                val nodeId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterNode(it.value, permissionManager, authentication, nodeId, permission)
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
                       authentication: Authentication, modelIdentifier: ModelIdentifier,
                       permission: EndpointModelElementPermission): Node? {

            if (permissionManager.hasEndpointModelElementPermission(authentication.userDetails(),
                            modelIdentifier, permission)) {
                return node
            }
            var n = Node()
            n.online = n.online
            n.implements = n.implements

            node.objects.forEach {
                val objId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterObject(it.value, permissionManager, authentication, objId, permission)
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
                         authentication: Authentication, modelIdentifier: ModelIdentifier,
                         permission: EndpointModelElementPermission): CloudioObject? {

            if (permissionManager.hasEndpointModelElementPermission(authentication.userDetails(),
                            modelIdentifier, permission)) {
                return obj
            }

            var o = CloudioObject()
            o.conforms = obj.conforms

            obj.objects.forEach {
                val objId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterObject(it.value, permissionManager, authentication, objId, permission)
                if (temp is CloudioObject) {
                    o.objects[it.key] = temp
                }
            }

            obj.attributes.forEach {
                val attrId = ModelIdentifier(modelIdentifier.toString() + "/" + it.key)
                val temp = filterAttribute(it.value, permissionManager, authentication, attrId, permission)
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
                            authentication: Authentication, modelIdentifier: ModelIdentifier,
                            permission: EndpointModelElementPermission): Attribute? {

            if (permissionManager.hasEndpointModelElementPermission(authentication.userDetails(),
                            modelIdentifier, permission)) {
                return attribute
            }

            return null
        }

        /**
         * Merge the noDataStructure in data
         * if an element does not exist in data, add it from noDataStructure
         */
        fun merge(data: Any?, noDataStructure: Any?): Any? {
            if(data == null){
                return noDataStructure
            }
            if(noDataStructure == null){
                return data
            }

            when (data) {
                is EndpointDataModel -> {
                    if (noDataStructure is EndpointDataModel){
                        return mergeEndpoint(data, noDataStructure)
                    }
                }
                is Node -> {
                    if (noDataStructure is Node){
                        return mergeNode(data, noDataStructure)
                    }
                }
                is CloudioObject -> {
                    if (noDataStructure is CloudioObject){
                        return mergeObject(data, noDataStructure)
                    }
                }
                is Attribute -> {
                    return data
                }
            }

            return null
        }

        fun mergeEndpoint(data: EndpointDataModel, noDataStructure: EndpointDataModel): EndpointDataModel {
            noDataStructure.nodes.forEach {
                val n = Node()
                n.implements = it.value.implements
                n.online = it.value.online


                data.nodes[it.key] = mergeNode(data.nodes.getOrDefault(it.key, n), it.value)
            }
            return data
        }

        fun mergeNode(data: Node, noDataStructure: Node): Node {
            noDataStructure.objects.forEach {
                val o = CloudioObject()
                o.conforms = it.value.conforms

                data.objects[it.key] = mergeObject(data.objects.getOrDefault(it.key, o), it.value)
            }
            return data
        }

        fun mergeObject(data: CloudioObject, noDataStructure: CloudioObject): CloudioObject {
            noDataStructure.objects.forEach {
                val o = CloudioObject()
                o.conforms = it.value.conforms

                data.objects[it.key] = mergeObject(data.objects.getOrDefault(it.key, o), it.value)
            }

            noDataStructure.attributes.forEach {
                data.attributes.putIfAbsent(it.key, it.value)
            }

            return data
        }
    }
}