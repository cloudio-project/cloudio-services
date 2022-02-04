package ch.hevs.cloudio.cloud.extension

import ch.hevs.cloudio.cloud.dao.InfluxQueryAPI
import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import java.util.*

fun EndpointDataModel.findAttribute(path: Stack<String>): Attribute? = if (path.size > 1) {
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

fun EndpointDataModel.fillAttributesFromInfluxDB(influx: InfluxQueryAPI, endpointUuid: UUID) {
    this.nodes.forEach {
        it.value.fillAttributesFromInfluxDB(influx, "${endpointUuid}/${it.key}")
    }
}

fun Node.fillAttributesFromInfluxDB(influx: InfluxQueryAPI, topic: String) {
    this.objects.forEach {
        it.value.fillAttributesFromInfluxDB(influx, "$topic/${it.key}")
    }
}

fun CloudioObject.fillAttributesFromInfluxDB(influx: InfluxQueryAPI, topic: String) {
    this.attributes.forEach {
        it.value.fillFromInfluxDB(influx, "$topic/${it.key}")
    }
    this.objects.forEach {
        it.value.fillAttributesFromInfluxDB(influx, "$topic/${it.key}")
    }
}

fun Attribute.fillFromInfluxDB(influx: InfluxQueryAPI, topic: String) {
    // TODO: Implement with new influx API.

    /*influx.query(Query("SELECT value from \"${topic.replace('/', '.')}\" ORDER BY desc LIMIT 1", database)).results[0].series?.let {
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
    }*/
}
