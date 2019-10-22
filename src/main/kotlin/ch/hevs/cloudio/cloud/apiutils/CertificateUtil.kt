package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio.cloud.internalservice.CertificateFromKey
import ch.hevs.cloudio.cloud.internalservice.UuidAndPublicKey
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.env.Environment
import java.util.*

object CertificateUtil{

    fun createCertificateAndKey(rabbitTemplate: RabbitTemplate, certificateAndKeyRequest: CertificateAndKeyRequest): CertificateAndPrivateKey{
        val uuid = UUID.fromString(certificateAndKeyRequest.endpointUuid)

        val mapper = ObjectMapper().registerModule(KotlinModule())

        //set waiting time to infinite --> wait until the Certificate manager service turns on
        rabbitTemplate.setReplyTimeout(-1)
        val certificateAndPrivateKey = CertificateAndPrivateKey("","")
        val uuidString = mapper.writeValueAsString(uuid)
        val certificateFromUUID = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "endpointKey-certificatePair", uuidString) as String?
        mapper.readerForUpdating(certificateAndPrivateKey).readValue(certificateFromUUID) as CertificateAndPrivateKey?

        println(certificateAndPrivateKey.certificate)
        println(certificateAndPrivateKey.privateKey)
        //reset waiting time
        rabbitTemplate.setReplyTimeout(0)

        return certificateAndPrivateKey
    }

    fun createCertificateFromKey(rabbitTemplate: RabbitTemplate, certificateFromKeyRequest: CertificateFromKeyRequest): CertificateFromKey{
        val uuidAndPublicKey = UuidAndPublicKey(UUID.fromString(certificateFromKeyRequest.endpointUuid),
                certificateFromKeyRequest.publicKey)

        val mapper = ObjectMapper().registerModule(KotlinModule())
        //set waiting time to infinite --> wait until the Certificate manager service turns on
        rabbitTemplate.setReplyTimeout(-1)

        val certificateOutput = CertificateFromKey("")
        val uuidAndPublicKeyString = mapper.writeValueAsString(uuidAndPublicKey)
        val certificateFromUuidKey = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "certificateFromPublicKey",uuidAndPublicKeyString) as String?
        mapper.readerForUpdating(certificateOutput).readValue(certificateFromUuidKey) as CertificateFromKey?
        println(mapper.writeValueAsString(uuidAndPublicKey))
        println(certificateOutput.certificate)
        //reset waiting time
        rabbitTemplate.setReplyTimeout(0)

        return certificateOutput
    }
    //CertificateAndKeyRequest CertificateFromKeyRequest

    fun getCaCertificate(environment: Environment): CaCertificateRequest{
        return CaCertificateRequest(environment.getRequiredProperty("cloudio.caCertificate"))
    }
}