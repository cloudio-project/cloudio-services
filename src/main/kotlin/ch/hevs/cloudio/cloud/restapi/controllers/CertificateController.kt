package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio.cloud.internalservice.CertificateFromKey
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioBadRequestException
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1")
class CertificateController(var environment: Environment, var userGroupRepository: UserGroupRepository, var userRepository: UserRepository){

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/createCertificateAndKey", method = [RequestMethod.GET])
    fun createCertificateAndKey(@RequestBody certificateAndKeyRequest: CertificateAndKeyRequest): CertificateAndPrivateKey {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = certificateAndKeyRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))!= Permission.OWN)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")
        return CertificateUtil.createCertificateAndKey(rabbitTemplate, certificateAndKeyRequest)
    }

    @RequestMapping("/createCertificateAndKeyZip", method = [RequestMethod.GET])
    fun createCertificateAndKeyZip(@RequestBody certificateAndKeyZipRequest: CertificateAndKeyZipRequest): ResponseEntity<UrlResource> {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = certificateAndKeyZipRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))!= Permission.OWN)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")
        val pathToReturn = CertificateUtil.createCertificateAndKeyZip(rabbitTemplate, certificateAndKeyZipRequest)!!.replace("\"","")

        val resource = UrlResource("file:$pathToReturn")

        val contentType = "application/zip"

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource)

    }

    @RequestMapping("/createCertificateFromKey", method = [RequestMethod.GET])
    fun createCertificateFromKey(@RequestBody certificateFromKeyRequest: CertificateFromKeyRequest): CertificateFromKey {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromGroup(userRepository.findById(userName).get().permissions,
                        userRepository.findById(userName).get().userGroups,
                        userGroupRepository)
        val genericTopic = certificateFromKeyRequest.endpointUuid + "/#"
        if(PermissionUtils.getHigherPriorityPermission(permissionMap, genericTopic.split("/"))!=Permission.OWN)
            throw CloudioBadRequestException("You don't have permission to  access this endpoint")
        return CertificateUtil.createCertificateFromKey(rabbitTemplate, certificateFromKeyRequest)

    }

    @RequestMapping("/getCaCertificate", method = [RequestMethod.GET])
    fun getCaCertificate(): CaCertificate {
        return CertificateUtil.getCaCertificate(environment)
    }
}