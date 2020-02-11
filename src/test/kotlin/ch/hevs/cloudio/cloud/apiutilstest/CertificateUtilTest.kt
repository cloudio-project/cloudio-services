package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.config.CloudioCertificateManagerProperties
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.extension.toBigInteger
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.security.cert.X509Certificate
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CertificateUtilTest {

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @Autowired
    private lateinit var endpointEntityRepository: EndpointEntityRepository

    @Autowired
    private lateinit var certificateManagerProperties: CloudioCertificateManagerProperties

    private lateinit var endpointParameters: EndpointParameters


    @BeforeAll
    fun setup() {
        val friendlyName = "JuanPablitoTheEndpoint"
        endpointParameters = EndpointManagementUtil.createEndpoint(endpointEntityRepository, EndpointCreateRequest(friendlyName))
    }

    @AfterAll
    fun cleanUp() {
        endpointEntityRepository.deleteById(endpointParameters.endpointUuid)
    }

    @Test
    fun getCaCertificate() {
        //get CA certificate
        val caCertPem = CertificateUtil.getCaCertificate(certificateManagerProperties)
        convertToX509Certificate(caCertPem.caCertificate) //if doesn't throw error, format of cacert is correct
    }

    @Test
    fun createCertificateAndKey() {
        //get CA certificate
        val caCertPem = CertificateUtil.getCaCertificate(certificateManagerProperties)
        val caCert = convertToX509Certificate(caCertPem.caCertificate)

        //create certificate and key for created endpoint
        val certificateAndPrivateKey = CertificateUtil.createCertificateAndKey(rabbitTemplate, CertificateAndKeyRequest(endpointParameters.endpointUuid))
        val certificate = convertToX509Certificate(certificateAndPrivateKey.certificate)
        certificateTests(certificate!!, endpointParameters.endpointUuid, caCert!!)
    }

    @Test
    fun createCertificateFromKey() {
        //get CA certificate
        val caCertPem = CertificateUtil.getCaCertificate(certificateManagerProperties)
        val caCert = convertToX509Certificate(caCertPem.caCertificate)

        //create certificate and from key for created endpoint
        val certificateFromKey = CertificateUtil.createCertificateFromKey(rabbitTemplate, CertificateFromKeyRequest(endpointParameters.endpointUuid, "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsyvgJ1hmPEJ2lvMZ3jmAdvYNqgZuvLXcADn/RnYMsW3PW+q123Ouw23IdD5y/BQVnNvx8E4PKdSg13zZMTBTylvstR90neL+3h6wEB6b9yYtu0ivC7uCBE80fj0LPG5ntKy7igPhMzr6yT3653orMM8DLg3kNLnV9x59rLylWpsQKkxwagRyiZaZqn5E3yx0tDODgbNWxSbeaiPFrrplPLsB5WPB32CM8VVkvbdIwHjOTMK5J3KP/iOSGkjfo4mGNRw45LE7LV88m/s/z6Xv51Gnd9GKw/uPCdv1vHM7GsaBS0iR0IDKPuByTCAa3REuG/WYGQ99nqglbl5fC+lcKQIDAQAB-----END PUBLIC KEY-----"))
        val certificate = convertToX509Certificate(certificateFromKey.certificate)
        certificateTests(certificate!!, endpointParameters.endpointUuid, caCert!!)

    }

    @Test
    fun createCertificateAndKeyZip() {
        //create zip of certificate
        val path = CertificateUtil.createCertificateAndKeyZip(rabbitTemplate, CertificateAndKeyZipRequest(endpointParameters.endpointUuid, LibraryLanguage.JAVA))

        var myFile = File(path!!.replace("\"", ""))
        assert(myFile.exists())

        val fileInZip = readZip(path.replace("\"", ""))
        assert(fileInZip.contains("/ca-cert.jks"))
        assert(fileInZip.contains("/${endpointParameters.endpointUuid}.properties"))
        assert(fileInZip.contains("/${endpointParameters.endpointUuid}.p12"))

        //delete generated properties p12 and jks zip file
        myFile = File("${endpointParameters.endpointUuid}.properties")
        if (myFile.exists())
            myFile.delete()

        myFile = File("${endpointParameters.endpointUuid}.p12")
        if (myFile.exists())
            myFile.delete()

        myFile = File("${endpointParameters.endpointUuid}.zip")
        if (myFile.exists())
            myFile.delete()
    }

    fun convertToX509Certificate(pem: String): X509Certificate? {
        val reader = StringReader(pem)
        val pr = PEMParser(reader)

        val x509CertificateHolder = pr.readObject() as X509CertificateHolder
        val x509Certificate = JcaX509CertificateConverter().getCertificate(x509CertificateHolder)
        return x509Certificate
    }

    fun certificateTests(certificate: X509Certificate, uuid: String, caCert: X509Certificate) {
        certificate.checkValidity()

        // check that uuid(in certificate serial number) is the same
        assert(certificate.serialNumber == UUID.fromString(uuid).toBigInteger())

        val now = Date(System.currentTimeMillis())
        //check that certificate beginning validity date is correct
        val cToday = Calendar.getInstance()
        cToday.time = now
        val cBefore = Calendar.getInstance()
        cBefore.time = certificate.notBefore
        assert(cToday.get(Calendar.YEAR) == cBefore.get(Calendar.YEAR))
        assert(cToday.get(Calendar.MONTH) == cBefore.get(Calendar.MONTH))
        assert(cToday.get(Calendar.DAY_OF_MONTH) == cBefore.get(Calendar.DAY_OF_MONTH))
        assert(cToday.get(Calendar.HOUR) == cBefore.get(Calendar.HOUR))
        assert(cToday.get(Calendar.MINUTE) == cBefore.get(Calendar.MINUTE))

        //check that certificate ending validity date is correct
        val c = Calendar.getInstance()
        c.time = certificate.notBefore
        c.add(Calendar.YEAR, 100)
        val notAfterDate = c.time
        assert(notAfterDate == certificate.notAfter)

        assert(certificate.issuerX500Principal == caCert.subjectX500Principal)
    }

    fun readZip(zipFilePath: String): Set<String> {
        val toReturn: MutableSet<String> = mutableSetOf()
        try {
            val zipFile = ZipFile(zipFilePath)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name = entry!!.name
                val type = if (entry.isDirectory) "DIR" else "FILE"
                if (type == "FILE")
                    toReturn.add(name)
            }

            zipFile.close();
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return toReturn
    }
}
