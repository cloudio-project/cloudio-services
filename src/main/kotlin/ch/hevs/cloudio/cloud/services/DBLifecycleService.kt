package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("lifecycle-db", "default")
class DBLifecycleService(
        private val endpointRepository: EndpointRepository,
        serializationFormats: Collection<SerializationFormat>
) : AbstractLifecycleService(serializationFormats) {
    private val log = LogFactory.getLog(DBLifecycleService::class.java)

    override fun endpointIsOnline(uuid: String, dataModel: EndpointDataModel) = endpointRepository.findById(UUID.fromString(uuid)).ifPresent { endpoint ->
        endpoint.online = true
        endpoint.dataModel.version = dataModel.version
        endpoint.dataModel.supportedFormats = dataModel.supportedFormats
        dataModel.nodes.forEach {
            it.value.online = true
            endpoint.dataModel.nodes[it.key] = it.value
        }
        endpointRepository.save(endpoint)
    }

    override fun endpointIsOffline(uuid: String) = endpointRepository.findById(UUID.fromString(uuid)).ifPresent { endpoint ->
        endpoint.online = false
        endpoint.dataModel.nodes.forEach {
            it.value.online = false
        }
        endpointRepository.save(endpoint)
    }

    override fun nodeAdded(uuid: String, nodeName: String, node: Node) = endpointRepository.findById(UUID.fromString(uuid)).ifPresent { endpoint ->
        node.online = true
        endpoint.dataModel.nodes[nodeName] = node
        endpointRepository.save(endpoint)
    }

    override fun nodeRemoved(uuid: String, nodeName: String) = endpointRepository.findById(UUID.fromString(uuid)).ifPresent { endpoint ->
        endpoint.dataModel.nodes[nodeName]?.run {
            online = false
            endpointRepository.save(endpoint)
        }
    }
}
