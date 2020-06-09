package ch.hevs.cloudio.cloud.internalservice.certificatemanager

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.util.*

@Service
@Lazy
class CertificateManagerProxy(private val rabbitTemplate: RabbitTemplate) {
    init {
        // TODO: It is not clear (to me) it amqpTemplate is a singleton or not. If it is a singleton we should avoid
        //       to change the reply timeout globally.
        rabbitTemplate.setReplyTimeout(15000)
    }

    fun getCACertificate(): String {
        return rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::getCACertificate", "") as String
    }

    fun generateEndpointKeyAndCertificate(endpointUUID: UUID): Pair<String, String> {
        val response = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::generateEndpointKeyAndCertificate",
                GenerateEndpointKeyAndCertificateRequest(endpointUUID))
                as GenerateEndpointKeyAndCertificateResponse
        if (endpointUUID != response.endpointUUID) {
            throw RuntimeException("UUID does not match")
        }
        return Pair(response.certificate, response.privateKey)
    }

    fun generateEndpointCertificateFromPublicKey(endpointUUID: UUID, publicKeyPEM: String): String {
        val response = rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "CertificateManagerService::generateEndpointCertificateFromPublicKey",
                GenerateEndpointCertificateFromPublicKeyRequest(endpointUUID, publicKeyPEM))
                as GenerateEndpointCertificateFromPublicKeyResponse
        if (endpointUUID != response.endpointUUID) {
            throw RuntimeException("UUID does not match")
        }
        return response.certificatePEM
    }
}
