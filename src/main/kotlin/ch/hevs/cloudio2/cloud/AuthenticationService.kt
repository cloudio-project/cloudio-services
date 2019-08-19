package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio2.cloud.model.Authority
import ch.hevs.cloudio2.cloud.model.Permission
import ch.hevs.cloudio2.cloud.repo.authentication.EndpointCredential
import ch.hevs.cloudio2.cloud.repo.authentication.EndpointCredentialRepository
import ch.hevs.cloudio2.cloud.repo.authentication.User
import ch.hevs.cloudio2.cloud.repo.authentication.UserRepository
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class AuthenticationService(var userRepository: UserRepository, var endpointCredentialRepository: EndpointCredentialRepository) {

    companion object {
        private val log = LogFactory.getLog(AuthenticationService::class.java)
    }

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    private var encoder: PasswordEncoder = BCryptPasswordEncoder()

    @PostConstruct
    fun initialize()
    {
        if (userRepository.count() == 0L)
            userRepository.save(User("root",
                    encoder.encode("123456"),
                    mapOf("toto" to Permission.GRANT),
                    setOf(Authority.BROKER_ADMINISTRATION, Authority.HTTP_ACCESS)))

        if(endpointCredentialRepository.count() == 0L) {
            val uuid = UUID.randomUUID()
            endpointCredentialRepository.save(EndpointCredential(uuid.toString(),
                    "root",
                    mapOf("toto" to Permission.GRANT),
                    setOf(Authority.BROKER_ADMINISTRATION, Authority.HTTP_ACCESS)))
            certificateFromUUIDThreadedPrint(uuid)
        }
    }

    @RabbitListener(bindings = [QueueBinding(value=Queue("authentication"),
            exchange = Exchange(value = "authentication", type = ExchangeTypes.FANOUT, ignoreDeclarationExceptions = "true"))])
    fun authenticate (message: Message): String
    {
        val action =  message.messageProperties.headers["action"]?.toString()
        val id =  message.messageProperties.headers["username"].toString()

        try {
            return when (action) {
                "login" -> {
                    val password = message.messageProperties.headers["password"]?.toString()

                    if(password != null)
                    {   //authentication with password --> User
                        log.info("User authentication with password")

                        val user = userRepository.findById(id)
                        if (user.isPresent && encoder.matches(password, user.get().passwordHash)) {
                            user.get().authorities.joinToString(separator = ",") {it.value}
                        } else {
                            "refused"
                        }
                    }
                    else
                    {   //authentication with certificates --> Endpoint
                        log.info("Endpoint authentication with certificates")

                        val endpoint = endpointCredentialRepository.findById(id)
                        if (endpoint.isPresent) {
                            endpoint.get().authorities.joinToString(separator = ",") {it.value}
                        } else {
                            "refused"
                        }
                    }
                }
                "check_vhost" -> {
                    val vhost = message.messageProperties.headers["vhost"]?.toString()
                    when (vhost) {
                        "/" -> "allow"
                        else -> {
                            log.warn("$action $vhost")
                            "deny"
                        }
                    }
                }
                "check_resource" -> {
                    val resource = message.messageProperties.headers["resource"]?.toString()
                    val permission = Permission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())

                    when (resource) {
                        "queue" -> if (permission.value <= Permission.CONFIGURE.value) "allow" else "deny"

                        "exchange" -> if (permission.value <= Permission.WRITE.value) "allow" else "deny"

                        else -> "deny"
                    }
                }
                "check_topic" -> {
                    val permission = Permission.valueOf((message.messageProperties.headers["permission"] as String).toUpperCase())
                    val routingKey = (message.messageProperties.headers["routing_key"] as String).split(".")
                    val password = message.messageProperties.headers["password"]?.toString()

                    if(routingKey.size<2)
                       return "deny"

                    val endpointPermission =  when(password) {
                        null -> {
                            endpointCredentialRepository.findById(id).get().permissions.getOrDefault(routingKey[1], Permission.DENY)
                        }
                        else -> userRepository.findById(id).get().permissions.getOrDefault(routingKey[1], Permission.DENY)
                    }

                    when {
                        endpointPermission == Permission.DENY -> "deny"
                        routingKey[0][0] != '@' -> "deny"
                        endpointPermission.value >= permission.value -> "allow"
                        else -> "deny"
                    }
                }
                else -> {
                    log.info("Unexpected header action, connection denied")
                    "deny"
                }
            }
        } catch (exception: Exception) {
            log.error("Exception during authentication message handling", exception)
        }
        return "allow"
    }

    fun certificateFromUUID(uuid:UUID):CertificateAndPrivateKey?
    {
        //get certificate from certificate manager service
        return rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "certificate-manager", uuid) as CertificateAndPrivateKey?
    }

    fun certificateFromUUIDThreadedPrint(uuid:UUID)
    {
        Thread(Runnable {
            //set waiting time to infinite --> wait until the Certificate manager service turns on
            rabbitTemplate.setReplyTimeout(-1)
            val certificateAndPrivateKey = certificateFromUUID(uuid)
            println(certificateAndPrivateKey?.certificate)
            println(certificateAndPrivateKey?.privateKey)
            //reset waiting time
            rabbitTemplate.setReplyTimeout(0)
        }).start()
    }
}
