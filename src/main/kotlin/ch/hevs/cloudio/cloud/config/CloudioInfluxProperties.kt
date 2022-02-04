package ch.hevs.cloudio.cloud.config

import com.influxdb.client.WriteOptions
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "cloudio.influx")
data class CloudioInfluxProperties(
        var url: String = "http://influx:8086",
        var token: String = "",
        var organisation: String = "ECS",
        val batchSize: Int = WriteOptions.DEFAULT_BATCH_SIZE,
        val flushIntervalMs: Int = WriteOptions.DEFAULT_FLUSH_INTERVAL,
        val bufferLimit: Int = WriteOptions.DEFAULT_BUFFER_LIMIT
)
