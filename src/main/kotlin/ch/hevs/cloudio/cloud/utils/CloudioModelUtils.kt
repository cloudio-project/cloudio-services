package ch.hevs.cloudio.cloud.utils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.model.Node
import java.util.*

object CloudioModelUtils {

    fun findAttributeInEndpoint(endpoint: Endpoint, path: Stack<String>): Attribute? {
        if (path.size > 1) {
            val node = endpoint.nodes[path.pop()]
            if (node != null) {
                return findAttributeInNode(node, path)
            }
        }
        return null
    }

    fun findAttributeInNode(node: Node, path: Stack<String>): Attribute? {
        if (path.size > 1) {
            val obj = node.objects[path.pop()]
            if (obj != null) {
                return findAttributeInObject(obj, path)
            }
        }

        return null
    }

    fun findAttributeInObject(obj: CloudioObject, path: Stack<String>): Attribute? {
        return if (path.size >= 1) {
            if (path.size == 1) {
                obj.attributes[path.pop()]
            } else {
                val childObj = obj.objects[path.pop()]
                if (childObj != null) {
                    findAttributeInObject(childObj, path)
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    fun findObjectInNode(node: Node, path: Stack<String>): CloudioObject? {
        if (path.size > 1) {
            val obj = node.objects[path.pop()]
            if (obj != null) {
                return findObjectInObject(obj, path)
            }
        }else if(path.size == 1) {
            return node.objects[path.pop()]
        }

        return null
    }

    fun findObjectInObject(obj: CloudioObject, path: Stack<String>): CloudioObject? {
        return if (path.size >= 1) {
            if (path.size == 1) {
                obj.objects[path.pop()]
            } else {
                val childObj = obj.objects[path.pop()]
                if (childObj != null) {
                    findObjectInObject(childObj, path)
                } else {
                    null
                }
            }
        } else {
            null
        }
    }
}