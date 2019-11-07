package ch.hevs.cloudio.cloud.internalservice

import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateFromKeyRequest
import ch.hevs.cloudio.cloud.utils.toBigInteger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
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
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.StringReader
import java.io.StringWriter
import java.security.*
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.*


@Service
@Profile("certificate-manager", "default")
class CertificateManagerService(environment: Environment) {

    private val mapper: ObjectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    private val bouncyCastle: BouncyCastleProvider = BouncyCastleProvider()
    private val keyPairGenerator: KeyPairGenerator

    private val privateKey: PrivateKey
    private val certificate: X509Certificate

    init {
        Security.addProvider(bouncyCastle)

        keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC").apply {
            initialize(2048, SecureRandom())
        }

        privateKey = JcaPEMKeyConverter().getPrivateKey(PEMParser(
                StringReader(environment.getRequiredProperty("cloudio.caPrivateKey"))
        ).readObject() as PrivateKeyInfo)

        certificate = JcaX509CertificateConverter().getCertificate(PEMParser(
                StringReader(environment.getRequiredProperty("cloudio.caCertificate"))
        ).readObject() as X509CertificateHolder)
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(name = "cloudio.service.internal.endpointKey-certificatePair"),
                exchange = Exchange(name = "cloudio.service.internal", type = ExchangeTypes.DIRECT),
                key = ["endpointKey-certificatePair"]
        )])
    fun generateEndpointKeyAndCertificatePair(certificateAndKeyRequestString: String): String{

        val certificateAndKeyRequest= CertificateAndKeyRequest("")

        mapper.readerForUpdating(certificateAndKeyRequest).readValue(certificateAndKeyRequestString) as CertificateAndKeyRequest
        val uuid = UUID.fromString(certificateAndKeyRequest.endpointUuid)

        val keyPair = keyPairGenerator.generateKeyPair()
        val subject = X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, uuid.toString()).build()
        val serial = uuid.toBigInteger()
        val now = Date(System.currentTimeMillis())
        val expires = Calendar.getInstance().run {
            time = now
            add(Calendar.YEAR, 100)
            time
        }
        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(privateKey)
        val builder = JcaX509v3CertificateBuilder(JcaX509CertificateHolder(certificate).subject, serial, now, expires, subject, keyPair.public)
        val certificate = JcaX509CertificateConverter().getCertificate(builder.build(signer))

        val toReturn = CertificateAndPrivateKey(
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

        return mapper.writeValueAsString(toReturn)
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(name = "cloudio.service.internal.certificateFromPublicKey"),
                exchange = Exchange(name = "cloudio.service.internal", type = ExchangeTypes.DIRECT),
                key = ["certificateFromPublicKey"]
        )])
    fun generateEndpointCertificateFromPublicKey(certificateFromKeyRequestString: String): String{

        val certificateFromKeyRequest = CertificateFromKeyRequest("","")

        mapper.readerForUpdating(certificateFromKeyRequest).readValue(certificateFromKeyRequestString) as CertificateFromKeyRequest
        val uuid = UUID.fromString(certificateFromKeyRequest.endpointUuid)

        val subject = X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, uuid.toString()).build()
        val serial = uuid.toBigInteger()
        val now = Date(System.currentTimeMillis())
        val expires = Calendar.getInstance().run {
            time = now
            add(Calendar.YEAR, 100)
            time
        }
        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(privateKey)
        val builder = JcaX509v3CertificateBuilder(JcaX509CertificateHolder(certificate).subject, serial, now, expires, subject, getKey(certificateFromKeyRequest.publicKey))
        val certificate = JcaX509CertificateConverter().getCertificate(builder.build(signer))

        val toReturn = CertificateFromKey(StringWriter().let {
                        JcaPEMWriter(it).run {
                            writeObject(certificate)
                            flush()
                            close()
                        }
                        it
                    }.toString())

        return mapper.writeValueAsString(toReturn)
    }

    fun getKey(key: String): PublicKey? {
        val formattedKey = key.replace("\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")

        val keyBytes: ByteArray = Base64.getDecoder().decode(formattedKey)//, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        return keyFactory.generatePublic(spec)
    }

}
