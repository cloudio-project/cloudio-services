package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.config.CloudioInfluxProperties
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.WriteOptions
import com.influxdb.client.write.Point
import org.springframework.stereotype.Repository

@Repository
class InfluxWriteAPI(
    influxClient: InfluxDBClient,
    private val properties: CloudioInfluxProperties
    ) {
    private val api = influxClient.makeWriteApi(
        WriteOptions.Builder()
        .batchSize(properties.batchSize)
        .flushInterval(properties.flushIntervalMs)
        .build())

    fun writePoint(bucket: String, point: Point) = api.writePoint(bucket, properties.organisation, point)
}
