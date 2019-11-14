package ch.hevs.cloudio.cloud.services

import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.repo.authentication.EndpointParametersRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.utils.PermissionUtils
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

@Service
@Profile("authentication", "default")
class AuthenticationService(var userRepository: UserRepository,var userGroupRepository: UserGroupRepository, var endpointParametersRepository: EndpointParametersRepository) {

    companion object {
        private val log = LogFactory.getLog(AuthenticationService::class.java)
        private val uuidPattern = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b".toRegex()
    }

    @Autowired
    val rabbitTemplate = RabbitTemplate()

    private var encoder: PasswordEncoder = BCryptPasswordEncoder()

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

                    if(routingKey.size<2)
                       return "deny"

                    when(uuidPattern.matches(id)) {
                        true -> {
                            if(id == routingKey[1] && endpointParametersRepository.existsById(id))
                                "allow"
                            else
                                "deny"
                        }
                        false -> {
                            val permissionMap = PermissionUtils
                                    .permissionFromUserAndGroup(id, userRepository, userGroupRepository)

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
}
