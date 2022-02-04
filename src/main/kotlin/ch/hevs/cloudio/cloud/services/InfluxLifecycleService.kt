package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.dao.InfluxWriteAPI
import ch.hevs.cloudio.cloud.model.*
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.apache.commons.logging.LogFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("lifecycle-influx", "default")
class InfluxLifecycleService(
    private val influx: InfluxWriteAPI
) : AbstractLifecycleService() {
    private val log = LogFactory.getLog(InfluxLifecycleService::class.java)

    override fun endpointIsOnline(endpointUUID: UUID, dataModel: EndpointDataModel) {
        influx.writePoint("$endpointUUID", Point.measurement("lifecycle")
            .addTag("event", "online")
            .addField("version", dataModel.version)
        )
        dataModel.nodes.writeAttributeValues(endpointUUID)
    }

    override fun endpointIsOffline(endpointUUID: UUID) {
        influx.writePoint("$endpointUUID", Point.measurement("lifecycle")
            .addTag("event", "offline")
        )
    }

    override fun nodeAdded(endpointUUID: UUID, nodeName: String, node: Node) {
        influx.writePoint("$endpointUUID", Point.measurement("lifecycle")
            .addTag("event", "node-added")
            .addField("name", nodeName)
        )
        node.writeAttributeValues(endpointUUID, nodeName)
    }

    override fun nodeRemoved(endpointUUID: UUID, nodeName: String) {
        influx.writePoint("$endpointUUID", Point.measurement("lifecycle")
            .addTag("event", "node-removed")
            .addField("name", nodeName)
        )
    }

    private fun Map<String, Node>.writeAttributeValues(endpointUUID: UUID) {
        forEach { (name, node) -> node.writeAttributeValues(endpointUUID , name) }
    }

    private fun Node.writeAttributeValues(endpointUUID: UUID, nodeName: String) {
        objects.forEach { (name, obj) -> obj.writeAttributeValues(endpointUUID, "$nodeName.$name") }
    }

    private fun CloudioObject.writeAttributeValues(endpointUUID: UUID, objectId: String) {
        objects.forEach { (name, obj) -> obj.writeAttributeValues(endpointUUID, "$objectId.$name") }
        attributes.forEach { (name, attribute) -> attribute.writeAttributeValue(endpointUUID, "$objectId.$name") }
    }

    private fun Attribute.writeAttributeValue(endpointUUID: UUID, attributeId: String) {
        if (timestamp != null && constraint != AttributeConstraint.Static) {

            // create the influxDB point
            val point = Point.measurement(attributeId)
                .time((timestamp!! * (1000.0) * 1000.0).toLong(), WritePrecision.MS)
                .addTag("event", "online")
                .addTag("constraint", constraint.toString())
                .addTag("type", type.toString())

            try {
                // complete the point depending on attribute type
                when (type) {
                    AttributeType.Boolean -> point.addField("value", value as Boolean)
                    AttributeType.Integer -> point.addField("value", value as Int)
                    AttributeType.Number -> {
                        if (value is Int) {
                            value = (value as Int).toFloat()
                        }
                        point.addField("value", value as Number)
                    }
                    AttributeType.String -> point.addField("value", value as String)
                    else -> {
                    }
                }

                influx.writePoint("$endpointUUID", point)

            } catch (e: ClassCastException) {
                log.error("The attribute $attributeId has been updated with wrong data type")
            }
        }
    }
}
