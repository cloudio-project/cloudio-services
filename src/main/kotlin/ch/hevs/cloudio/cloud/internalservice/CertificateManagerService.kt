package ch.hevs.cloudio.cloud.internalservice

import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyZipRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateFromKeyRequest
import ch.hevs.cloudio.cloud.apiutils.LibraryLanguage
import ch.hevs.cloudio.cloud.config.CloudioCertificateManagerProperties
import ch.hevs.cloudio.cloud.extension.toBigInteger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
import java.io.*
import java.security.*
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
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
                        name = "cloudio.service.internal.endpointKey-certificatePair"
                ),
                exchange = Exchange(
                        name = "cloudio.service.internal",
                        type = ExchangeTypes.DIRECT
                ),
                key = ["endpointKey-certificatePair"]
        )
    ])
    fun generateEndpointKeyAndCertificatePair(certificateAndKeyRequest: CertificateAndKeyRequest): CertificateAndPrivateKey? {
        try {
            val uuid = UUID.fromString(certificateAndKeyRequest.endpointUuid)

            val (certificate, keyPair) = createAndSignEndpointCertificate(uuid)

            return CertificateAndPrivateKey(
                    StringWriter().let {
                        JcaPEMWriter(it).run {
                            writeObject(certificate)
                            flush()
                            close()
                        }
                        it
                    }.toString(),
                    StringWriter().let {
                        JcaPEMWriter(it).run {
                            writeObject(keyPair.private)
                            flush()
                            close()
                        }
                        it
                    }.toString()
            )
        } catch (exception: Exception) {
            log.error("Exception during endpointKey-certificatePair", exception)
        }
        return null
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(
                        name = "cloudio.service.internal.certificateFromPublicKey"
                ),
                exchange = Exchange(
                        name = "cloudio.service.internal",
                        type = ExchangeTypes.DIRECT
                ),
                key = ["certificateFromPublicKey"]
        )
    ])
    fun generateEndpointCertificateFromPublicKey(certificateFromKeyRequest: CertificateFromKeyRequest): CertificateFromKey? {
        try {
            val uuid = UUID.fromString(certificateFromKeyRequest.endpointUuid)

            val certificate = signCertificate(uuid, certificateFromKeyRequest.publicKey.toPublicKey())

            return CertificateFromKey(StringWriter().let {
                JcaPEMWriter(it).run {
                    writeObject(certificate)
                    flush()
                    close()
                }
                it
            }.toString())
        } catch (exception: Exception) {
            log.error("endpointKey-certificateFromPublicKey", exception)
        }
        return null
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(
                        name = "cloudio.service.internal.endpointKey-certificatePairZip"
                ),
                exchange = Exchange(
                        name = "cloudio.service.internal",
                        type = ExchangeTypes.DIRECT
                ),
                key = ["endpointKey-certificatePairZip"]
        )
    ])
    fun generateEndpointKeyCertificateZip(certificateAndKeyZipRequest: CertificateAndKeyZipRequest): ByteArray? {
        try {
            val uuid = UUID.fromString(certificateAndKeyZipRequest.endpointUuid)
            val (certificate, privateKey) = createAndSignEndpointCertificate(uuid)
            val password = generateRandomPassword()

            val output = ByteArrayOutputStream()
            val zip = ZipOutputStream(output)

            // Write properties file.
            zip.putNextEntry(ZipEntry("cloud.io/${certificateAndKeyZipRequest.endpointUuid}.properties"))
            Properties().apply {
                setProperty("ch.hevs.cloudio.endpoint.ssl.clientPassword", password)
                setProperty("ch.hevs.cloudio.endpoint.ssl.authorityPassword", password)
            }.store(zip, "")
            zip.closeEntry()

            // Add certificate authority keystore.
            zip.putNextEntry(ZipEntry("cloud.io/authority.jks"))
            zip.writeJKSFile(password, this.certificate)
            zip.closeEntry()

            // Add client P12 file.
            zip.putNextEntry(ZipEntry("cloud.io/${certificateAndKeyZipRequest.endpointUuid}.p12"))
            zip.writePKCS12File(password, privateKey, certificate)
            zip.closeEntry()

            zip.close()
            return output.toByteArray()
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
}
