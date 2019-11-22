package ch.hevs.cloudio.cloud.restapi.controllers

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio.cloud.internalservice.CertificateFromKey
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.EndpointEntityRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions.CLOUDIO_BLOCKED_ENDPOINT
import ch.hevs.cloudio.cloud.utils.PermissionUtils
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.io.UrlResource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.handler.MappedInterceptor
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/api/v1")
class CertificateController(var environment: Environment, var userGroupRepository: UserGroupRepository, var userRepository: UserRepository, var endpointEntityRepository: EndpointEntityRepository){

    companion object {
        private val log = LogFactory.getLog(CertificateController::class.java)
    }

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    @RequestMapping("/createCertificateAndKey", method = [RequestMethod.GET])
    fun createCertificateAndKey(@RequestBody certificateAndKeyRequest: CertificateAndKeyRequest): CertificateAndPrivateKey {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = certificateAndKeyRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if(endpointGeneralPermission?.permission == Permission.OWN) {
            if(endpointEntityRepository.findByIdOrNull(certificateAndKeyRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CLOUDIO_BLOCKED_ENDPOINT)
            else
                return CertificateUtil.createCertificateAndKey(rabbitTemplate, certificateAndKeyRequest)
        }
        else{
            if(endpointGeneralPermission==null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @RequestMapping("/createCertificateAndKeyZip", method = [RequestMethod.GET])
    fun createCertificateAndKeyZip(@RequestBody certificateAndKeyZipRequest: CertificateAndKeyZipRequest): ResponseEntity<UrlResource> {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = certificateAndKeyZipRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if(endpointGeneralPermission?.permission == Permission.OWN) {
            if (endpointEntityRepository.findByIdOrNull(certificateAndKeyZipRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CLOUDIO_BLOCKED_ENDPOINT)
            else{
                val pathToReturn = CertificateUtil.createCertificateAndKeyZip(rabbitTemplate, certificateAndKeyZipRequest)!!.replace("\"", "")

                val resource = UrlResource("file:$pathToReturn")

                val contentType = "application/zip"

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.filename + "\"")
                        .header("EndpointUuid", certificateAndKeyZipRequest.endpointUuid)
                        .body(resource)
            }
        }
        else{
            if(endpointGeneralPermission==null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @Bean
    fun interceptor(): MappedInterceptor {
        return MappedInterceptor(arrayOf("/api/v1/createCertificateAndKeyZip"), object : HandlerInterceptor {
            @Throws(Exception::class)
            override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
                if(response.status == 200){
                    val endpointUuid = response.getHeaders("EndpointUuid").toMutableList()[0]
                    try{
                        var myFile = File("$endpointUuid.properties")
                        if (myFile.exists())
                            myFile.delete()

                        myFile = File("$endpointUuid.p12")
                        if (myFile.exists())
                            myFile.delete()

                        myFile = File("$endpointUuid.zip")
                        if (myFile.exists())
                            myFile.delete()
                    }catch (e: Exception){
                        log.error("Exception while deleting old certificate files", e)
                    }
                }
            }
        })
    }

    @RequestMapping("/createCertificateFromKey", method = [RequestMethod.GET])
    fun createCertificateFromKey(@RequestBody certificateFromKeyRequest: CertificateFromKeyRequest): CertificateFromKey {
        val userName = SecurityContextHolder.getContext().authentication.name

        val permissionMap = PermissionUtils
                .permissionFromUserAndGroup(userName, userRepository, userGroupRepository)
        val genericTopic = certificateFromKeyRequest.endpointUuid + "/#"
        val endpointGeneralPermission = permissionMap.get(genericTopic)
        if(endpointGeneralPermission?.permission == Permission.OWN)
            if (endpointEntityRepository.findByIdOrNull(certificateFromKeyRequest.endpointUuid)!!.blocked)
                throw CloudioHttpExceptions.BadRequestException(CLOUDIO_BLOCKED_ENDPOINT)
            else
                return CertificateUtil.createCertificateFromKey(rabbitTemplate, certificateFromKeyRequest)
        else{
            if(endpointGeneralPermission==null)
                throw CloudioHttpExceptions.BadRequestException("This endpoint doesn't exist")
            else
                throw CloudioHttpExceptions.BadRequestException("You don't own this endpoint")
        }
    }

    @RequestMapping("/getCaCertificate", method = [RequestMethod.GET])
    fun getCaCertificate(): CaCertificate {
        return CertificateUtil.getCaCertificate(environment)
    }
}