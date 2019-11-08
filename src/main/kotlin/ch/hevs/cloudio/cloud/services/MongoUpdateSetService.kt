package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractUpdateSetService
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.utils.CloudioModelUtils
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile("update-mongo", "default")
class MongoUpdateSetService(val endpointEntityRepository: EndpointEntityRepository) : AbstractUpdateSetService(){

    override fun attributeUpdatedSet(attributeId: String, attribute: Attribute, prefix: String) {
        val path = Stack<String>()
        path.addAll(attributeId.split(".").toList().reversed())
        if (path.size >= 3) {
            val id = path.pop()
            val endpointEntity = endpointEntityRepository.findByIdOrNull(id)
            if (endpointEntity != null) {
                val node = endpointEntity.endpoint.nodes[path.pop()]
                if (node != null) {
                    val existingAttribute = CloudioModelUtils.findAttributeInNode(node, path)
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
}