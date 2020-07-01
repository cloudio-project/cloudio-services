package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("lifecycle-mongo", "default")
class DBLifecycleService(
        private val endpointRepository: EndpointRepository,
        serializationFormats: Collection<SerializationFormat>
) : AbstractLifecycleService(serializationFormats) {

    companion object {
        private val log = LogFactory.getLog(DBLifecycleService::class.java)
    }

    override fun endpointIsOnline(endpointId: String, endpoint: EndpointDataModel) {
        val endpointEntity = endpointRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            endpointEntity.online = true
            endpointEntity.dataModel.version = endpoint.version
            endpointEntity.dataModel.supportedFormats = endpoint.supportedFormats
            endpointEntity.dataModel.nodes.clear()
            endpointEntity.dataModel.nodes.putAll(endpoint.nodes)
            endpointRepository.save(endpointEntity)
        } else
            log.error("Endpoint tried to use @online on $endpointId whose hasn't been created by using cloud.iO API")
    }

    override fun endpointIsOffline(endpointId: String) {
        val endpointEntity = endpointRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            endpointEntity.online = false
            endpointRepository.save(endpointEntity)
        }
    }

    override fun nodeAdded(endpointId: String, nodeName: String, node: Node) {
        val endpointEntity = endpointRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            endpointEntity.dataModel.nodes[nodeName] = node
            endpointRepository.save(endpointEntity)
        }
    }

    override fun nodeRemoved(endpointId: String, nodeName: String) {
        val endpointEntity = endpointRepository.findByIdOrNull(UUID.fromString(endpointId))
        if (endpointEntity != null) {
            // TODO: It would be better to mark the node as not connected rather than removing it from the datamodel!
            endpointEntity.dataModel.nodes.remove(nodeName)
            endpointRepository.save(endpointEntity)
        }
    }
}
