package ch.hevs.cloudio.cloud.internalservice

import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyZipRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateFromKeyRequest
import ch.hevs.cloudio.cloud.apiutils.LibraryLanguage
import ch.hevs.cloudio.cloud.utils.toBigInteger
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.logging.LogFactory
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
import java.io.*
import java.security.*
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@Service
@Profile("certificate-manager", "default")
class CertificateManagerService(environment: Environment) {


    companion object {
        private val log = LogFactory.getLog(CertificateManagerService::class.java)
    }

    private val mapper: ObjectMapper by lazy { ObjectMapper().registerModule(KotlinModule()) }

    private val bouncyCastle: BouncyCastleProvider = BouncyCastleProvider()
    private val keyPairGenerator: KeyPairGenerator

    private val privateKey: PrivateKey
    private val certificate: X509Certificate

    private val caCertificatePath: String
    private val caCertificatePassword: String

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

        caCertificatePath = environment.getRequiredProperty("cloudio.caCertificateJksPath")
        caCertificatePassword = environment.getRequiredProperty("cloudio.caCertificateJksPassword")

    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(name = "cloudio.service.internal.endpointKey-certificatePair"),
                exchange = Exchange(name = "cloudio.service.internal", type = ExchangeTypes.DIRECT),
                key = ["endpointKey-certificatePair"]
        )])
    fun generateEndpointKeyAndCertificatePair(certificateAndKeyRequestString: String): String? {
        try {
            val certificateAndKeyRequest = CertificateAndKeyRequest("")

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
        } catch (exception: Exception) {
            log.error("Exception during endpointKey-certificatePair", exception)
        }
        return null
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(name = "cloudio.service.internal.endpointKey-certificatePairZip"),
                exchange = Exchange(name = "cloudio.service.internal", type = ExchangeTypes.DIRECT),
                key = ["endpointKey-certificatePairZip"]
        )])
    fun generateEndpointKeyCertificateZip(certificateAndKeyRequestString: String): String? {
        try {
            val certificateAndKeyZipRequest = CertificateAndKeyZipRequest("", LibraryLanguage.JAVA)

            mapper.readerForUpdating(certificateAndKeyZipRequest).readValue(certificateAndKeyRequestString) as CertificateAndKeyZipRequest
            val uuid = UUID.fromString(certificateAndKeyZipRequest.endpointUuid)

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

            //create random password for p12 keystore
            val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val password: String = List(15) { alphabet.random() }.joinToString("")

            createPKCS12File(certificateAndKeyZipRequest.endpointUuid, password, keyPair, certificate)

            when (certificateAndKeyZipRequest.libraryLanguage) {
                LibraryLanguage.JAVA -> {
                    //properties file for java librarie
                    val propertiesContent = "ch.hevs.cloudio.endpoint.hostUri=ssl://localhost:8883\n" +
                            "ch.hevs.cloudio.endpoint.ssl.authorityCert=file:ABSOLUTE_PATH/ca-cert.jks\n" +
                            "ch.hevs.cloudio.endpoint.ssl.clientCert=file:ABSOLUTE_PATH/${certificateAndKeyZipRequest.endpointUuid}.p12\n" +
                            "ch.hevs.cloudio.endpoint.ssl.clientPassword=$password\n" +
                            "ch.hevs.cloudio.endpoint.ssl.authorityPassword=$caCertificatePassword\n" +
                            "ch.hevs.cloudio.endpoint.persistence=memory\n" +
                            "ch.hevs.cloudio.endpoint.jobs.folder=ABSOLUTE_PATH"
                    File("${certificateAndKeyZipRequest.endpointUuid}.properties").writeText(propertiesContent)

                    //list for the properties, p12 and cacert files
                    val files: Array<String> = arrayOf(File("${certificateAndKeyZipRequest.endpointUuid}.properties").absolutePath,
                            File("${certificateAndKeyZipRequest.endpointUuid}.p12").absolutePath,
                            File(caCertificatePath).absolutePath)
                    //zip all files and return the serialized File object of zip file
                    val zipFile = File("${certificateAndKeyZipRequest.endpointUuid}.zip")
                    val out = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
                    for (file in files) {
                        val fi = FileInputStream(file)
                        val origin = BufferedInputStream(fi)
                        val entry = ZipEntry(file.substring(file.lastIndexOf("/")))
                        out.putNextEntry(entry)
                        origin.copyTo(out, 1024)
                        origin.close()
                    }
                    out.close()
                    return mapper.writeValueAsString(zipFile)
                }
                else -> {

                }
            }


            return mapper.writeValueAsString(true)
        } catch (exception: Exception) {
            log.error("endpointKey-certificatePairZip", exception)
        }
        return null
    }

    @RabbitListener(bindings = [
        QueueBinding(
                value = Queue(name = "cloudio.service.internal.certificateFromPublicKey"),
                exchange = Exchange(name = "cloudio.service.internal", type = ExchangeTypes.DIRECT),
                key = ["certificateFromPublicKey"]
        )])
    fun generateEndpointCertificateFromPublicKey(certificateFromKeyRequestString: String): String? {
        try {
            val certificateFromKeyRequest = CertificateFromKeyRequest("", "")

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
        } catch (exception: Exception) {
            log.error("endpointKey-certificateFromPublicKey", exception)
        }
        return null
    }

    fun getKey(key: String): PublicKey? {
        //convert PEM string Key to java.security.PublicKey
        val formattedKey = key.replace("\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")

        val keyBytes: ByteArray = Base64.getDecoder().decode(formattedKey)//, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        return keyFactory.generatePublic(spec)
    }

    fun createPKCS12File(endPointUuidString: String, keyStorePassword: String, pair: KeyPair, certificate: X509Certificate): String? {
        val pkcs12 = KeyStore.getInstance("PKCS12")
        pkcs12.load(null, null)

        pkcs12.setKeyEntry("", pair.private, "".toCharArray(), arrayOf(certificate))

        try {
            val p12File = File("$endPointUuidString.p12")
            val p12 = FileOutputStream(p12File)
            pkcs12.store(p12, keyStorePassword.toCharArray())
            return p12File.absoluteFile.toString()
        } catch (e: Exception) {
            log.error("Couldn't create P12 file", e)
            e.printStackTrace()
            return null
        }

    }

}
