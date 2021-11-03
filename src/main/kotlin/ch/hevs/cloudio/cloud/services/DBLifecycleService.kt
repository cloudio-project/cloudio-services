package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.dao.EndpointRepository
import ch.hevs.cloudio.cloud.model.AttributeConstraint
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("lifecycle-db", "default")
class DBLifecycleService(
        private val endpointRepository: EndpointRepository
) : AbstractLifecycleService() {
    private val log = LogFactory.getLog(DBLifecycleService::class.java)

    override fun endpointIsOnline(endpointUUID: UUID, dataModel: EndpointDataModel) = endpointRepository.findById(endpointUUID).ifPresent { endpoint ->
        endpoint.online = true
        endpoint.dataModel.version = dataModel.version
        endpoint.dataModel.messageFormatVersion = dataModel.messageFormatVersion
        endpoint.dataModel.supportedFormats = dataModel.supportedFormats
        dataModel.nodes.forEach {
            it.value.online = true
            endpoint.dataModel.nodes[it.key] = it.value
        }
        endpoint.dataModel.nodes.removeDynamicAttributeValues()
        endpointRepository.save(endpoint)
    }

    override fun endpointIsOffline(endpointUUID: UUID) = endpointRepository.findById(endpointUUID).ifPresent { endpoint ->
        endpoint.online = false
        endpoint.dataModel.nodes.forEach {
            it.value.online = false
        }
        endpointRepository.save(endpoint)
    }

    override fun nodeAdded(endpointUUID: UUID, nodeName: String, node: Node) = endpointRepository.findById(endpointUUID).ifPresent { endpoint ->
        node.online = true
        node.removeDynamicAttributeValues()
        endpoint.dataModel.nodes[nodeName] = node
        endpointRepository.save(endpoint)
    }

    override fun nodeRemoved(endpointUUID: UUID, nodeName: String) = endpointRepository.findById(endpointUUID).ifPresent { endpoint ->
        endpoint.dataModel.nodes[nodeName]?.run {
            online = false
            endpointRepository.save(endpoint)
        }
    }

    private fun Map<String,Node>.removeDynamicAttributeValues() {
        forEach { (_, node) -> node.removeDynamicAttributeValues() }
    }

    private fun Node.removeDynamicAttributeValues() {
        objects.forEach { (_, obj) -> obj.removeDynamicAttributeValues() }
    }

    private fun CloudioObject.removeDynamicAttributeValues() {
        objects.forEach { (_, obj) -> obj.removeDynamicAttributeValues() }
        attributes.forEach { (_, attribute) -> if (attribute.constraint != AttributeConstraint.Static) {
            attribute.value = null
            attribute.timestamp = null
        } }
    }
}
