package ch.hevs.cloudio.cloud.extension

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.EndpointDataModel
import ch.hevs.cloudio.cloud.model.Node
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxQLQueryApi
import com.influxdb.client.domain.InfluxQLQuery
import com.influxdb.query.InfluxQLQueryResult
import java.text.ParseException
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

fun EndpointDataModel.fillAttributesFromInfluxDB(influx: InfluxDBClient, database: String, endpointUuid: UUID) {
    this.nodes.forEach {
        it.value.fillAttributesFromInfluxDB(influx, database, "${endpointUuid}/${it.key}")
    }
}

fun Node.fillAttributesFromInfluxDB(influx: InfluxDBClient, database: String, topic: String) {
    this.objects.forEach {
        it.value.fillAttributesFromInfluxDB(influx, database, "$topic/${it.key}")
    }
}

fun CloudioObject.fillAttributesFromInfluxDB(influx: InfluxDBClient, database: String, topic: String) {
    this.attributes.forEach {
        it.value.fillFromInfluxDB(influx, database, "$topic/${it.key}")
    }
    this.objects.forEach {
        it.value.fillAttributesFromInfluxDB(influx, database, "$topic/${it.key}")
    }
}

fun Attribute.fillFromInfluxDB(influx: InfluxDBClient, database: String, topic: String) {
    val queryApi: InfluxQLQueryApi = influx.influxQLQueryApi

    val myQuery: InfluxQLQueryResult? = queryApi.query(
        InfluxQLQuery(
            "SELECT value from \"${topic.replace('/', '.')}\" ORDER BY desc LIMIT 1",
            database
        )
    )

    if (myQuery != null) {
        myQuery.results[0].series.let {
            var dt: Date? = null

            try {
                var timestamp = it[0].values[0].values[0].toString()
                dt = Date(timestamp.toLong())
            } catch (exception: ParseException) {
                exception.printStackTrace()
            }

            if (dt != null) {
                this.value = it[0].values[0].values[1]
                this.timestamp = dt.time.toDouble()
            }
        }
    }
}
