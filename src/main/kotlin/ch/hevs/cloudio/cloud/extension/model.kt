package ch.hevs.cloudio.cloud.extension

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun Endpoint.findAttribute(path: Stack<String>): Attribute? = if (path.size > 1) {
    this.nodes[path.pop()]?.findAttribute(path)
} else {
    null
}

fun Node.findAttribute(path: Stack<String>): Attribute? = if (path.size > 1) {
    this.objects[path.pop()]?.findAttribute(path)
} else {
    null
}

fun CloudioObject.findAttribute(path: Stack<String>): Attribute? = when {
    path.size == 1 -> this.attributes[path.pop()]
    path.size >= 1 -> this.objects[path.pop()]?.findAttribute(path)
    else -> null
}

fun Node.findObject(path: Stack<String>): CloudioObject? = when {
    path.size == 1 -> this.objects[path.pop()]
    path.size >= 1 -> this.objects[path.pop()]?.findObject(path)
    else -> null
}

fun CloudioObject.findObject(path: Stack<String>): CloudioObject? = when {
    path.size == 1 -> this.objects[path.pop()]
    path.size >= 1 -> this.objects[path.pop()]?.findObject(path)
    else -> null
}

fun EndpointEntity.fillAttributesFromInfluxDB(influx: InfluxDB, database: String) {
    this.endpoint.nodes.forEach {
        it.value.fillAttributesFromInfluxDB(influx, database, "${this.endpointUuid}/${it.key}")
    }
}

fun Node.fillAttributesFromInfluxDB(influx: InfluxDB, database: String, topic: String) {
    this.objects.forEach {
        it.value.fillAttributesFromInfluxDB(influx, database, "$topic/${it.key}")
    }
}

fun CloudioObject.fillAttributesFromInfluxDB(influx: InfluxDB, database: String, topic: String) {
    this.attributes.forEach {
        it.value.fillFromInfluxDB(influx, database, "$topic/${it.key}")
    }
    this.objects.forEach {
        it.value.fillAttributesFromInfluxDB(influx, database, "$topic/${it.key}")
    }
}

fun Attribute.fillFromInfluxDB(influx: InfluxDB, database: String, topic: String) {
    influx.query(Query("SELECT value from \"${topic.replace('/', '.')}\" ORDER BY desc LIMIT 1", database)).results[0].series?.let {
        var dt: Date? = null
        try {
            dt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(it[0].values[0][0].toString())
        } catch (exception: ParseException) {
            try {
                dt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(it[0].values[0][0].toString())
            } catch (exception: ParseException) {
                exception.printStackTrace()
            }
        }
        if (dt != null) {
            this.value = it[0].values[0][1]
            this.timestamp = dt.time.toDouble()
        }
    }
}