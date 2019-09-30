package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.abstractservices.AbstractUpdateService
import ch.hevs.cloudio2.cloud.model.Attribute
import ch.hevs.cloudio2.cloud.model.CloudioObject
import ch.hevs.cloudio2.cloud.model.Node
import ch.hevs.cloudio2.cloud.repo.EndpointEntityRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile("update-mongo", "default")
class MongoUpdateService(val endpointEntityRepository: EndpointEntityRepository) : AbstractUpdateService(){

    override fun attributeUpdated(attributeId: String, attribute: Attribute) {
        println(attributeId)
        val path = Stack<String>()
        path.addAll(attributeId.split(".").toList().reversed())
        if (path.size >= 3) {
            val id = path.pop()
            val endpointEntity = endpointEntityRepository.findByIdOrNull(id)
            if (endpointEntity != null) {
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
        if (path.size > 1) {
            val obj = node.objects[path.pop()]
            if (obj != null) {
                return findAttributeInObject(obj, path)
            }
        }

        return null
    }

    private fun findAttributeInObject(obj: CloudioObject, path: Stack<String>): Attribute? {
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
}