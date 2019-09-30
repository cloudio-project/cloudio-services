package ch.hevs.cloudio2.cloud

import ch.hevs.cloudio2.cloud.internalservice.CertificateAndPrivateKey
import ch.hevs.cloudio2.cloud.model.Authority
import ch.hevs.cloudio2.cloud.model.Permission
import ch.hevs.cloudio2.cloud.model.PrioritizedPermission
import ch.hevs.cloudio2.cloud.model.Priority
import ch.hevs.cloudio2.cloud.repo.authentication.EndpointParameters
import ch.hevs.cloudio2.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio2.cloud.repo.authentication.User
import ch.hevs.cloudio2.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio2.cloud.utils.PermissionUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.logging.LogFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
@Profile("authentication", "default")
class AuthenticationService(var userRepository: UserRepository, var endpointParametersRepository: EndpointParametersRepository) {

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
                    mapOf("toto" to PrioritizedPermission(Permission.GRANT, Priority.HIGHEST)),
                    setOf(Authority.BROKER_ADMINISTRATION, Authority.HTTP_ACCESS)))

        if(endpointParametersRepository.count() == 0L) {
            val uuid = UUID.randomUUID()
            endpointParametersRepository.save(EndpointParameters(uuid.toString(),
                    "toto"))
            certificateFromUUIDThreadedPrint(uuid)

            endpointParametersRepository.save(EndpointParameters("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4",
                    "test"))
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

                        val endpoint = endpointParametersRepository.findById(id)
                        if (endpoint.isPresent) {
                            "allow"
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
                    val UUIDpattern = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()

                    if(routingKey.size<2)
                       return "deny"

                    when(UUIDpattern.matches(id)) {
                        true -> {
                            if(id == routingKey[1] && endpointParametersRepository.existsById(id))
                                "allow"
                            else
                                "deny"
                        }
                        false -> {
                            val permissionMap = userRepository.findById(id).get().permissions
                            val topicFilter = routingKey.drop(1) //drop the @...

                            //check if there is permission linked to topic
                            val endpointPermission = PermissionUtils.getHigherPriorityPermission(permissionMap, topicFilter)

                            when {
                                endpointPermission == Permission.DENY -> "deny"
                                routingKey[0][0] != '@' -> "deny"
                                endpointPermission.value >= permission.value -> "allow"
                                else -> "deny"
                            }
                        }
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

    fun certificateFromUUID(uuid:UUID):String?
    {
        //get certificate from certificate manager service
        return rabbitTemplate.convertSendAndReceive("cloudio.service.internal",
                "certificate-manager", uuid) as String?
    }

    fun certificateFromUUIDThreadedPrint(uuid:UUID)
    {
        val mapper = ObjectMapper().registerModule(KotlinModule())

        Thread(Runnable {
            //set waiting time to infinite --> wait until the Certificate manager service turns on
            rabbitTemplate.setReplyTimeout(-1)
            val certificateAndPrivateKey = CertificateAndPrivateKey("","")
            mapper.readerForUpdating(certificateAndPrivateKey).readValue(certificateFromUUID(uuid)) as CertificateAndPrivateKey?

            println(certificateAndPrivateKey.certificate)
            println(certificateAndPrivateKey.privateKey)
            //reset waiting time
            rabbitTemplate.setReplyTimeout(0)
        }).start()
    }
}
