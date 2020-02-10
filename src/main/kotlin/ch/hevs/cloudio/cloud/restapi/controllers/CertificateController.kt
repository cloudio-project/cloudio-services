package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyZipRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateFromKeyRequest
import ch.hevs.cloudio.cloud.config.CloudioCertificateManagerProperties
import ch.hevs.cloudio.cloud.internalservice.certificatemanager.CertificateManagerProxy
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/api/v1")
class CertificateController(
        private val certificateManagerProperties: CloudioCertificateManagerProperties,
        private val userGroupRepository: UserGroupRepository,
        private val userRepository: UserRepository,
        private val endpointEntityRepository: EndpointEntityRepository,
        private val rabbitTemplate: RabbitTemplate,
        private val certificateManager: CertificateManagerProxy) {

    private val log = LogFactory.getLog(CertificateController::class.java)

    @RequestMapping("/createCertificateAndKey", method = [RequestMethod.POST])
    fun createCertificateAndKey(@RequestBody certificateAndKeyRequest: CertificateAndKeyRequest): ResponseEntity<ByteArray> {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = certificateAndKeyRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {
            if (endpointEntityRepository.findByIdOrNull(certificateAndKeyRequest.endpointUuid)!!.blocked) {
                throw CloudioHttpExceptions.BadRequest(CLOUDIO_BLOCKED_ENDPOINT)
            } else {
                val (password, pkcs12KeyStore) = certificateManager.generateEndpointKeyAndCertificate(UUID.fromString(certificateAndKeyRequest.endpointUuid))
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/x-pkcs12"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${certificateAndKeyRequest.endpointUuid}.p12\"")
                        .header("Endpoint", certificateAndKeyRequest.endpointUuid)
                        .header("Keystore-Passphrase", password)
                        .body(pkcs12KeyStore)
            }
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.NotFound("Endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.Forbidden("You don't own this endpoint")
        }
    }

    @RequestMapping("/createCertificateAndKeyZip", method = [RequestMethod.POST])
    fun createCertificateAndKeyZip(@RequestBody certificateAndKeyZipRequest: CertificateAndKeyZipRequest): ResponseEntity<ByteArray> {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = certificateAndKeyZipRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN) {
            if (endpointEntityRepository.findByIdOrNull(certificateAndKeyZipRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequest(CLOUDIO_BLOCKED_ENDPOINT)
            else {
                val archive = certificateManager.generateEndpointConfigurationArchive(UUID.fromString(certificateAndKeyZipRequest.endpointUuid),
                        certificateAndKeyZipRequest.libraryLanguage)

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/java-archive"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${certificateAndKeyZipRequest.endpointUuid}.jar\"")
                        .header("Endpoint", certificateAndKeyZipRequest.endpointUuid)
                        .body(archive)
            }
        } else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.NotFound("Endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.Forbidden("You don't own this endpoint")
        }
    }

    @RequestMapping("/createCertificateFromKey", method = [RequestMethod.POST])
    fun createCertificateFromKey(@RequestBody certificateFromKeyRequest: CertificateFromKeyRequest): HttpEntity<String> {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = certificateFromKeyRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if (endpointGeneralPermission?.permission == Permission.OWN)
            if (endpointEntityRepository.findByIdOrNull(certificateFromKeyRequest.endpointUuid)!!.blocked) {
                throw CloudioHttpExceptions.BadRequest(CLOUDIO_BLOCKED_ENDPOINT)
            } else {
                val certificate = certificateManager.generateEndpointCertificateFromPublicKey(UUID.fromString(certificateFromKeyRequest.endpointUuid),
                        certificateFromKeyRequest.publicKey)
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/x-x509-user-cert"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${certificateFromKeyRequest.endpointUuid}.pem\"")
                        .header("Endpoint", certificateFromKeyRequest.endpointUuid)
                        .body(certificate)
            }
        else {
            if (endpointGeneralPermission == null)
                throw CloudioHttpExceptions.BadRequest("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequest("You don't own this endpoint")
        }
    }

    @RequestMapping("/getCaCertificate", method = [RequestMethod.GET])
    fun getCaCertificate(): ResponseEntity<String> {
        val ca = certificateManager.getCACertificate()
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-x509-user-cert"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"authority.pem\"")
                .body(ca)
    }
}