package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.config.CloudioCertificateManagerProperties
import ch.hevs.cloudio.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio.cloud.internalservice.CertificateFromKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.env.Environment

object CertificateUtil {

    fun createCertificateAndKey(rabbitTemplate: RabbitTemplate, certificateAndKeyRequest: CertificateAndKeyRequest): CertificateAndPrivateKey {
        val mapper = ObjectMapper().registerModule(KotlinModule())

        //set waiting time to infinite --> wait until the Certificate manager service turns on
        rabbitTemplate.setReplyTimeout(15000)
        val certificateAndPrivateKey = CertificateAndPrivateKey("", "")
        val certificateAndKeyRequestString = mapper.writeValueAsString(certificateAndKeyRequest)
        val certificateFromUUID = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "endpointKey-certificatePair", certificateAndKeyRequestString) as String?
        mapper.readerForUpdating(certificateAndPrivateKey).readValue(certificateFromUUID) as CertificateAndPrivateKey?
        //reset waiting time
        rabbitTemplate.setReplyTimeout(0)

        return certificateAndPrivateKey
    }

    fun createCertificateAndKeyZip(rabbitTemplate: RabbitTemplate, certificateAndKeyZipRequest: CertificateAndKeyZipRequest): String? {
        val mapper = ObjectMapper().registerModule(KotlinModule())

        //set waiting time to infinite --> wait until the Certificate manager service turns on
        rabbitTemplate.setReplyTimeout(15000)
        val certificateAndKeyRequestString = mapper.writeValueAsString(certificateAndKeyZipRequest)
        val certificateZipFromUUID = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "endpointKey-certificatePairZip", certificateAndKeyRequestString) as String?
        //reset waiting time
        rabbitTemplate.setReplyTimeout(0)

        return certificateZipFromUUID

    }

    fun createCertificateFromKey(rabbitTemplate: RabbitTemplate, certificateFromKeyRequest: CertificateFromKeyRequest): CertificateFromKey {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        //set waiting time to infinite --> wait until the Certificate manager service turns on
        rabbitTemplate.setReplyTimeout(15000)

        val certificateOutput = CertificateFromKey("")
        val uuidAndPublicKeyString = mapper.writeValueAsString(certificateFromKeyRequest)
        val certificateFromUuidKey = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "certificateFromPublicKey", uuidAndPublicKeyString) as String?
        mapper.readerForUpdating(certificateOutput).readValue(certificateFromUuidKey) as CertificateFromKey?
        //reset waiting time
        rabbitTemplate.setReplyTimeout(0)

        return certificateOutput
    }
    //CertificateAndKeyRequest CertificateFromKeyRequest

    fun getCaCertificate(certificateManagerProperties: CloudioCertificateManagerProperties): CaCertificate {
        return CaCertificate(certificateManagerProperties.caCertificate)
    }
}
