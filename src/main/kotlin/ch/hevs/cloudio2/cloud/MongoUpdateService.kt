package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.abstractservices.AbstractUpdateService
import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.model.CloudioObject
import ch.hevs.cloudio2.cloud.model.Node
import ch.hevs.cloudio2.cloud.repo.EndpointEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*


@Service
class MongoUpdateService(val endpointEntityRepository: EndpointEntityRepository) : AbstractUpdateService(){

    override fun attributeUpdated(attributeId: String, attribute: Attribute) {
        val path = Stack<String>()
        path.addAll(attributeId.split(".").toList().reversed())
        if (path.size >= 3) {

            val id = path.pop()
            val endpointEntity = endpointEntityRepository.findByIdOrNull(id)
            if (endpointEntity != null && path.pop() == "nodes") {
                val node = endpointEntity.endpoint.nodes[path.pop()]
                if (node != null) {
                    val existingAttribute = findAttributeInNode(node, path)
                    if (existingAttribute != null) {
                        existingAttribute.timestamp = attribute.timestamp
                        existingAttribute.constraint = attribute.constraint
                        existingAttribute.type = attribute.type
                        existingAttribute.value = attribute.value
                        endpointEntityRepository.save(endpointEntity)
                    }
                }
            }
        }
    }

    private fun findAttributeInNode(node: Node, path: Stack<String>): Attribute? {
        if (path.size > 1 && path.pop() == "objects") {
            val obj = node.objects[path.pop()]
            if (obj != null) {
                return findAttributeInObject(obj, path)
            }
        }

        return null
    }

    private fun findAttributeInObject(obj: CloudioObject, path: Stack<String>): Attribute? {
        if (path.size > 1) {
            when (path.pop()) {
                "objects" -> {
                    val childObj = obj.objects[path.pop()]
                    if (childObj != null) {
                        return findAttributeInObject(childObj, path)
                    } else {
                        return null
                    }
                }
                "attributes" -> return obj.attributes[path.pop()]
                else -> return null
            }
        } else {
            return null
        }
    }
}