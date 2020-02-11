package ch.hevs.cloudio.cloud.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "cloudio.influx")
data class CloudioInfluxProperties(
        val database: String = "cloudio",
        val batchIntervalMs: Int = 3000,
        val batchSize: Int = 2000
)
