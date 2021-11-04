package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.*
import org.apache.commons.logging.LogFactory
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Profile("lifecycle-influx", "default")
class InfluxLifecycleService(
    private val influx: InfluxDB,
    private val influxProperties: CloudioInfluxProperties
) : AbstractLifecycleService() {
    private val log = LogFactory.getLog(InfluxLifecycleService::class.java)

    override fun endpointIsOnline(endpointUUID: UUID, dataModel: EndpointDataModel) {
        influx.write(
            influxProperties.database, "autogen", Point
                .measurement(endpointUUID.toString())
                .addField("event", "online")
                .build()
        )
        dataModel.nodes.writeAttributeValues(endpointUUID.toString())
    }

    override fun endpointIsOffline(endpointUUID: UUID) {
        influx.write(
            influxProperties.database, "autogen", Point
                .measurement(endpointUUID.toString())
                .addField("event", "offline")
                .build()
        )
    }

    override fun nodeAdded(endpointUUID: UUID, nodeName: String, node: Node) {
        influx.write(
            influxProperties.database, "autogen", Point
                .measurement(endpointUUID.toString())
                .tag("node", nodeName)
                .addField("event", "added")
                .build()
        )
        node.writeAttributeValues("$endpointUUID.$nodeName")
    }

    override fun nodeRemoved(endpointUUID: UUID, nodeName: String) {
        influx.write(
            influxProperties.database, "autogen", Point
                .measurement(endpointUUID.toString())
                .tag("node", nodeName)
                .addField("event", "removed")
                .build()
        )
    }

    private fun Map<String, Node>.writeAttributeValues(endpointId: String) {
        forEach { (name, node) -> node.writeAttributeValues("$endpointId.$name") }
    }

    private fun Node.writeAttributeValues(nodeId: String) {
        objects.forEach { (name, obj) -> obj.writeAttributeValues("$nodeId.$name") }
    }

    private fun CloudioObject.writeAttributeValues(objectId: String) {
        objects.forEach { (name, obj) -> obj.writeAttributeValues("$objectId.$name") }
        attributes.forEach { (name, attribute) -> attribute.writeAttributeValue("$objectId.$name") }
    }

    private fun Attribute.writeAttributeValue(attributeId: String) {
        if (timestamp != null && constraint != AttributeConstraint.Static) {

            // create the influxDB point
            val point = Point
                .measurement(attributeId)
                .time((timestamp!! * (1000.0) * 1000.0).toLong(), TimeUnit.MICROSECONDS)
                .tag("constraint", constraint.toString())
                .tag("type", type.toString())

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
                //write the actual point in influx
                val myPoint = point.build()

                //if batch enabled, save point in set, either send it
                influx.write(influxProperties.database, "autogen", myPoint)

            } catch (e: ClassCastException) {
                log.error("The attribute $attributeId has been updated with wrong data type")
            }
        }
    }
}
