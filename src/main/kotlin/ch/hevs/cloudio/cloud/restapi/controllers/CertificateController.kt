package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.CaCertificateRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateAndKeyRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateFromKeyRequest
import ch.hevs.cloudio.cloud.apiutils.CertificateUtil
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
    fun getCaCertificate(): CaCertificateRequest {
        return CertificateUtil.getCaCertificate(environment)
    }
}