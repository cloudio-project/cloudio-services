package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.abstractservices.AbstractLifecycleService
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.model.Node
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
@Profile("lifecycle-influx", "default")
class InfluxLifecycleService(val env: Environment, val influx: InfluxDB) : AbstractLifecycleService() {

    //get database to write by environment property, has default value
    val database: String by lazy { env.getProperty("CLOUDIO_INFLUX_DATABASE", "CLOUDIO") }

    override fun endpointIsOffline(endpointId: String) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .addField("event", "offline")
                .build())
    }

    override fun endpointIsOnline(endpointId: String, endpoint: Endpoint) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .addField("event", "online")
                .build())
    }

    override fun nodeAdded(endpointId: String, nodeName: String, node: Node) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .tag("node", nodeName)
                .addField("event", "added")
                .build())
    }

    override fun nodeRemoved(endpointId: String, nodeName: String) {
        influx.write(database, "autogen", Point
                .measurement(endpointId)
                .tag("node", nodeName)
                .addField("event", "removed")
                .build())
    }

}