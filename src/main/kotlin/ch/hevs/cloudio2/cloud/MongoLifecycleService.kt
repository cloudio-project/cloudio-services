package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio2.cloud.model.Endpoint
import ch.hevs.cloudio2.cloud.model.Node
import ch.hevs.cloudio2.cloud.repo.EndpointEntity
import ch.hevs.cloudio2.cloud.repo.EndpointEntityRepository
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@Profile("lifecycle-mongo", "default")
class MongoLifecycleService(val endpointEntityRepository: EndpointEntityRepository): AbstractLifecycleService(){

    override  fun endpointIsOnline(endpointId: String, endpoint: Endpoint) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId) ?: EndpointEntity(endpointId)
        endpointEntity.online = true
        endpointEntity.endpoint = endpoint
        endpointEntityRepository.save(endpointEntity)
    }

    override  fun endpointIsOffline(endpointId: String) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId)
        if (endpointEntity != null) {
            endpointEntity.online = false
            endpointEntityRepository.save(endpointEntity)
        }
    }

    override  fun nodeAdded(endpointId: String, nodeName: String, node: Node) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId)
        if (endpointEntity != null) {
            endpointEntity.endpoint.nodes[nodeName] = node
            endpointEntityRepository.save(endpointEntity)
        }
    }

    override fun nodeRemoved(endpointId: String, nodeName: String) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(endpointId)
        if (endpointEntity != null) {
            endpointEntity.endpoint.nodes.remove(nodeName)
            endpointEntityRepository.save(endpointEntity)
        }
    }
}