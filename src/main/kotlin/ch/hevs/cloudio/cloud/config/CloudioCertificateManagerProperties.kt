package ch.hevs.cloudio.cloud.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "cloudio.cert-manager")
class CloudioCertificateManagerProperties(
        val caCertificate: String,
        val caPrivateKey: String,
        val keyAlgorithm: String = "RSA",
        val keySize: Int = 2048,
        val signAlgorithm: String = "SHA256WithRSA"
)
