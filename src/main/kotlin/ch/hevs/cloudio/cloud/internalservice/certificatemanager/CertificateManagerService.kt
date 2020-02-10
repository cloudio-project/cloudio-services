package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import ch.hevs.cloudio.cloud.config.CloudioCertificateManagerProperties
import ch.hevs.cloudio.cloud.extension.toBigInteger
import org.apache.commons.logging.LogFactory
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader
import java.io.StringWriter
import java.security.*
import java.security.cert.X509Certificate
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

    @RabbitListener(bindings = [
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

    @RabbitListener(bindings = [
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

            // Save the certificate and private key to a PKCS12 keystore.
            val password = request.password ?: generateRandomPassword()
            val pkcs12Data = ByteArrayOutputStream()
            pkcs12Data.writePKCS12File(password, keyPair, certificate)

            // Return keystore file and password.
            return GenerateEndpointKeyAndCertificateResponse(request.endpointUUID, password, pkcs12Data.toByteArray())
        } catch (exception: Exception) {
            log.error("Exception during CertificateManagerService::generateEndpointKeyAndCertificate", exception)
        }
        return null
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(
                        name = "cloudio.service.internal.CertificateManagerService::generateEndpointCertificateFromPublicKey"
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

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(
                        name = "cloudio.service.internal.CertificateManagerService::generateEndpointConfigurationArchive",
                        exclusive = "true"
                ),
                exchange = Exchange(
                        name = "cloudio.service.internal",
                        type = ExchangeTypes.DIRECT
                ),
                key = ["CertificateManagerService::generateEndpointConfigurationArchive"]
        )
    ])
    fun generateEndpointConfigurationArchive(request: GenerateEndpointConfigurationArchiveRequest): GenerateEndpointConfigurationArchiveResponse? {
        try {
            // Generate and sign certificate for endpoint.
            val (certificate, privateKey) = createAndSignEndpointCertificate(request.endpointUUID!!)

            // Save the configuration, the client certificate, private key and the authority keystore to a zip file.
            val password = generateRandomPassword()
            val output = ByteArrayOutputStream()
            val zip = ZipOutputStream(output)

            // Write properties file.
            zip.putNextEntry(ZipEntry("cloud.io/${request.endpointUUID}.properties"))
            Properties().apply {
                request.properties.forEach {
                    setProperty(it.key, it.value)
                }
                setProperty("ch.hevs.cloudio.endpoint.ssl.clientPassword", password)
                setProperty("ch.hevs.cloudio.endpoint.ssl.authorityPassword", password)
            }.store(zip, "")
            zip.closeEntry()

            // Add certificate authority keystore.
            zip.putNextEntry(ZipEntry("cloud.io/authority.jks"))
            zip.writeJKSFile(password, this.certificate)
            zip.closeEntry()

            // Add client P12 file.
            zip.putNextEntry(ZipEntry("cloud.io/${request.endpointUUID}.p12"))
            zip.writePKCS12File(password, privateKey, certificate)
            zip.closeEntry()

            zip.close()
            return GenerateEndpointConfigurationArchiveResponse(request.endpointUUID, request.language,
                    output.toByteArray())
        } catch (exception: Exception) {
            log.error("endpointKey-certificateFromPublicKey", exception)
        }
        return null
    }

    private fun generateRandomPassword(length: Int = 16): String {
        val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { alphabet.random() }.joinToString("")
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

    private fun OutputStream.writePKCS12File(keyStorePassword: String, pair: KeyPair, certificate: X509Certificate) {
        val pkcs12 = KeyStore.getInstance("PKCS12")
        pkcs12.load(null, null)
        pkcs12.setKeyEntry("", pair.private, "".toCharArray(), arrayOf(certificate))
        pkcs12.store(this, keyStorePassword.toCharArray())
    }

    private fun OutputStream.writeJKSFile(keyStorePassword: String, certificate: X509Certificate) {
        val jks = KeyStore.getInstance("JKS")
        jks.load(null, null)
        jks.setCertificateEntry("", certificate)
        jks.store(this, keyStorePassword.toCharArray())
    }

    private fun String.toPrivateKey() = JcaPEMKeyConverter().getPrivateKey(PEMParser(
            StringReader(this)).readObject() as PrivateKeyInfo)

    private fun String.toPublicKey() = JcaPEMKeyConverter().getPublicKey(PEMParser(
            StringReader(this)).readObject() as SubjectPublicKeyInfo)

    private fun String.toX509Certificate() = JcaX509CertificateConverter().getCertificate(PEMParser(
            StringReader(this)).readObject() as X509CertificateHolder)

    private fun X509Certificate.toPEMString() = StringWriter().also { writer ->
        JcaPEMWriter(writer).let {
            it.writeObject(this)
            it.flush()
            it.close()
        }
    }.toString()
}
