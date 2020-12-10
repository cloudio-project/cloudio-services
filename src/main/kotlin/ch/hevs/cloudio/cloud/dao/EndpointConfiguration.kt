package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.model.LogLevel
import javax.persistence.*

@Embeddable
data class EndpointConfiguration(
        @ElementCollection
        @JoinTable(name = "cloudio_endpoint_configuration_properties")
        @Column(name = "value", nullable = false)
        @MapKeyColumn(name="key")
        val properties: MutableMap<String, String> = mutableMapOf(),

        @Column(name = "pem_certificate", columnDefinition = "TEXT")
        var clientCertificate: String = "",

        @Column(name = "pem_private_key", columnDefinition = "TEXT")
        var privateKey: String = "",

        @Enumerated(EnumType.STRING)
        @Column(length = 32)
        var logLevel: LogLevel = LogLevel.ERROR
)
