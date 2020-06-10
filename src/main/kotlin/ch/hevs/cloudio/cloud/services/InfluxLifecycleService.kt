package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.serialization.SerializationFormat
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("lifecycle-influx", "default")
class InfluxLifecycleService(
        private val influx: InfluxDB,
        serializationFormats: Collection<SerializationFormat>,
        private val influxProperties: CloudioInfluxProperties) : AbstractLifecycleService(serializationFormats) {

    override fun endpointIsOffline(endpointId: String) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(endpointId)
                .addField("event", "offline")
                .build())
    }

    override fun endpointIsOnline(endpointId: String, endpoint: EndpointDataModel) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(endpointId)
                .addField("event", "online")
                .build())
    }

    override fun nodeAdded(endpointId: String, nodeName: String, node: Node) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(endpointId)
                .tag("node", nodeName)
                .addField("event", "added")
                .build())
    }

    override fun nodeRemoved(endpointId: String, nodeName: String) {
        influx.write(influxProperties.database, "autogen", Point
                .measurement(endpointId)
                .tag("node", nodeName)
                .addField("event", "removed")
                .build())
    }
}
