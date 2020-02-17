package ch.hevs.cloudio.cloud.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Profile

@ConstructorBinding
@ConfigurationProperties(prefix = "cloudio.cert-manager")
@Profile("certificate-manager", "default")
class CloudioCertificateManagerProperties(
        val caCertificate: String,
        val caPrivateKey: String,
        val keyAlgorithm: String = "RSA",
        val keySize: Int = 2048,
        val signAlgorithm: String = "SHA256WithRSA"
)
