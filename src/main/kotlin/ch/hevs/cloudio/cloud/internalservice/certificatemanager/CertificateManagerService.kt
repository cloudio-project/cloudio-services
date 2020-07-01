package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import ch.hevs.cloudio.cloud.config.CloudioCertificateManagerProperties
import ch.hevs.cloudio.cloud.extension.*
import org.apache.commons.logging.LogFactory
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.security.*
import java.security.cert.X509Certificate
import java.util.*

@Service
@Profile("certificate-manager", "default")
class CertificateManagerService(private val properties: CloudioCertificateManagerProperties) {
    private val log = LogFactory.getLog(CertificateManagerService::class.java)

    private val bouncyCastle = BouncyCastleProvider()
    private val keyPairGenerator: KeyPairGenerator

    private val privateKey: PrivateKey
    private val certificate: X509Certificate

    init {
        Security.addProvider(bouncyCastle)

        keyPairGenerator = KeyPairGenerator.getInstance(properties.keyAlgorithm, "BC").apply {
            initialize(properties.keySize, SecureRandom())
        }

        privateKey = properties.caPrivateKey.toPrivateKey()

        certificate = properties.caCertificate.toX509Certificate()
    }

    @RabbitListener(
            bindings = [
                QueueBinding(
                        value = Queue(
                                name = "cloudio.service.internal.CertificateManagerService::getCACertificate",
                                exclusive = "true"
                        ),
                        exchange = Exchange(
                                name = "cloudio.service.internal",
                                type = ExchangeTypes.DIRECT
                        ),
                        key = ["CertificateManagerService::getCACertificate"]
                )
            ])
    fun getCACertificate(): String {
        return certificate.toPEMString()
    }

    @RabbitListener(
            bindings = [
                QueueBinding(
                        value = Queue(
                                name = "cloudio.service.internal.CertificateManagerService::generateEndpointKeyAndCertificate",
                                exclusive = "true"
                        ),
                        exchange = Exchange(
                                name = "cloudio.service.internal",
                                type = ExchangeTypes.DIRECT
                        ),
                        key = ["CertificateManagerService::generateEndpointKeyAndCertificate"]
                )
            ])
    fun generateEndpointKeyAndCertificate(request: GenerateEndpointKeyAndCertificateRequest): GenerateEndpointKeyAndCertificateResponse? {
        try {
            // Generate and sign certificate for endpoint.
            val (certificate, keyPair) = createAndSignEndpointCertificate(request.endpointUUID!!)

            // Return certificate and private key.
            return GenerateEndpointKeyAndCertificateResponse(request.endpointUUID, certificate.toPEMString(), keyPair.private.toPEMString())
        } catch (exception: Exception) {
            log.error("Exception during CertificateManagerService::generateEndpointKeyAndCertificate", exception)
        }
        return null
    }

    @RabbitListener(
            bindings = [
                QueueBinding(
                        value = Queue(
                                name = "cloudio.service.internal.CertificateManagerService::generateEndpointCertificateFromPublicKey",
                                exclusive = "true"
                        ),
                        exchange = Exchange(
                                name = "cloudio.service.internal",
                                type = ExchangeTypes.DIRECT
                        ),
                        key = ["CertificateManagerService::generateEndpointCertificateFromPublicKey"]
                )
            ])
    fun generateEndpointCertificateFromPublicKey(request: GenerateEndpointCertificateFromPublicKeyRequest): GenerateEndpointCertificateFromPublicKeyResponse? {
        try {
            // Generate and sign certificate for endpoint using the public key passed.
            val certificate = signCertificate(request.endpointUUID!!, request.publicKeyPEM.toPublicKey())

            // Return certificate as PEM data.
            return GenerateEndpointCertificateFromPublicKeyResponse(request.endpointUUID, certificate.toPEMString())
        } catch (exception: Exception) {
            log.error("Exception during CertificateManagerService::generateEndpointCertificateFromPublicKey", exception)
        }
        return null
    }

    private fun createAndSignEndpointCertificate(uuid: UUID): Pair<X509Certificate, KeyPair> {
        val keyPair = keyPairGenerator.generateKeyPair()
        return Pair(signCertificate(uuid, keyPair.public), keyPair)
    }

    private fun signCertificate(uuid: UUID, publicKey: PublicKey): X509Certificate {
        val subject = X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, uuid.toString()).build()
        val serial = uuid.toBigInteger()
        val now = Date(System.currentTimeMillis())
        val expires = Calendar.getInstance().run {
            time = now
            add(Calendar.YEAR, 100)
            time
        }
        val signer = JcaContentSignerBuilder(properties.signAlgorithm).build(privateKey)
        val builder = JcaX509v3CertificateBuilder(JcaX509CertificateHolder(certificate).subject, serial, now, expires, subject, publicKey)
        return JcaX509CertificateConverter().getCertificate(builder.build(signer))
    }
}
