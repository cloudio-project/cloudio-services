package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import com.influxdb.client.InfluxDBClient
import org.springframework.stereotype.Repository

@Repository
class InfluxQueryAPI(
    influxClient: InfluxDBClient,
    private val properties: CloudioInfluxProperties
) {
    private val api = influxClient.queryApi

    fun query(query: String) = api.query(query, properties.organisation)
}
