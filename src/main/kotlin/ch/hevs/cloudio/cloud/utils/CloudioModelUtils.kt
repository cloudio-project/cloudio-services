package ch.hevs.cloudio.cloud.utils

import ch.hevs.cloudio.cloud.model.Attribute
import ch.hevs.cloudio.cloud.model.CloudioObject
import ch.hevs.cloudio.cloud.model.Endpoint
import ch.hevs.cloudio.cloud.model.Node
import ch.hevs.cloudio.cloud.repo.EndpointEntity
import org.influxdb.InfluxDB
import org.influxdb.dto.Query
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeParseException
import java.util.*

object CloudioModelUtils {

    fun findAttributeInEndpoint(endpoint: Endpoint, path: Stack<String>): Attribute? {
        if (path.size > 1) {
            val node = endpoint.nodes[path.pop()]
            if (node != null) {
                return findAttributeInNode(node, path)
            }
        }
        return null
    }

    fun findAttributeInNode(node: Node, path: Stack<String>): Attribute? {
        if (path.size > 1) {
            val obj = node.objects[path.pop()]
            if (obj != null) {
                return findAttributeInObject(obj, path)
            }
        }

        return null
    }

    fun findAttributeInObject(obj: CloudioObject, path: Stack<String>): Attribute? {
        return if (path.size >= 1) {
            if (path.size == 1) {
                obj.attributes[path.pop()]
            } else {
                val childObj = obj.objects[path.pop()]
                if (childObj != null) {
                    findAttributeInObject(childObj, path)
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    fun findObjectInNode(node: Node, path: Stack<String>): CloudioObject? {
        if (path.size > 1) {
            val obj = node.objects[path.pop()]
            if (obj != null) {
                return findObjectInObject(obj, path)
            }
        } else if (path.size == 1) {
            return node.objects[path.pop()]
        }

        return null
    }

    fun findObjectInObject(obj: CloudioObject, path: Stack<String>): CloudioObject? {
        return if (path.size >= 1) {
            if (path.size == 1) {
                obj.objects[path.pop()]
            } else {
                val childObj = obj.objects[path.pop()]
                if (childObj != null) {
                    findObjectInObject(childObj, path)
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    fun fillAttributeFromEndpoint(influx: InfluxDB, database: String, endpointEntity: EndpointEntity) {
        val topic = endpointEntity.endpointUuid + "/"

        for (node in endpointEntity.endpoint.nodes) {
            val topicNode = topic + node.key + "/"
            fillAttributeFromNode(influx, database, topicNode, node.value)
        }

    }

    fun fillAttributeFromNode(influx: InfluxDB, database: String, topic: String, node: Node) {

        for (cloudioObject in node.objects) {
            val topicObject = topic + cloudioObject.key + "/"
            fillAttributeFromObject(influx, database, topicObject, cloudioObject.value)
        }
    }

    fun fillAttributeFromObject(influx: InfluxDB, database: String, topic: String, cloudioObject: CloudioObject) {
        val innerTopic = topic
        for (attribute in cloudioObject.attributes) {
            val innerTopicAttribute = innerTopic + attribute.key

            fillAttributeFromTopic(influx, database, innerTopicAttribute, attribute.value)
        }

        for (innerCloudioObject in cloudioObject.objects) {
            val innerTopicObject = innerTopic + innerCloudioObject.key + "/"
            fillAttributeFromObject(influx, database, innerTopicObject, innerCloudioObject.value)
        }
    }

    fun fillAttributeFromTopic(influx: InfluxDB, database: String, topic: String, attribute: Attribute) {

        val queryResult = influx.query(Query("SELECT value FROM \"${topic.replace("/", ".")}\"  ORDER BY desc LIMIT 1", database))

        if (queryResult.results[0].series != null) {

            val sdfMili = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            var dt : Date? = null
            
            try{
                dt = sdf.parse(queryResult.results[0].series[0].values[0][0].toString())
            }catch (e: ParseException){
                try {
                    dt = sdfMili.parse(queryResult.results[0].series[0].values[0][0].toString())
                }catch(e2: ParseException){
                    e2.printStackTrace()
                }
            }
            
            if(dt!=null){
                val epoch = dt.time

                attribute.value = queryResult.results[0].series[0].values[0][1]
                attribute.timestamp = epoch.toDouble()
            }
        }
    }

}