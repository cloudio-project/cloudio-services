package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.restapi.controllers.CertificateController
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("lifecycle-mongo", "default")
class MongoLifecycleService(
        private val endpointEntityRepository: EndpointEntityRepository,
        serializationFormats: Collection<SerializationFormat>
) : AbstractLifecycleService(serializationFormats) {

    companion object {
        private val log = LogFactory.getLog(CertificateController::class.java)
    }

    override fun endpointIsOnline(endpointId: String, endpoint: Endpoint) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) { //To prevent endpoint creation without the api
            endpointEntity.online = true
            endpointEntity.endpoint = endpoint
            endpointEntityRepository.save(endpointEntity)
        } else
            log.error("Endpoint tried to use @online on $endpointId whose hasn't been created by using cloud.iO API")


    }

    override fun endpointIsOffline(endpointId: String) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            endpointEntity.online = false
            endpointEntityRepository.save(endpointEntity)
        }
    }

    override fun nodeAdded(endpointId: String, nodeName: String, node: Node) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            endpointEntity.endpoint.nodes[nodeName] = node
            endpointEntityRepository.save(endpointEntity)
        }
    }

    override fun nodeRemoved(endpointId: String, nodeName: String) {
        val endpointEntity = endpointEntityRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            endpointEntity.endpoint.nodes.remove(nodeName)
            endpointEntityRepository.save(endpointEntity)
        }
    }
}
