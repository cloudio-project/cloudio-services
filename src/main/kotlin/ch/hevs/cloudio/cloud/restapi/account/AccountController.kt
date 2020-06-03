package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.hibernate.Hibernate
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.security.Principal
import javax.transaction.Transactional

@Api(tags = ["Account"], description = "Allows user to interact with his own account.")
@RestController
@RequestMapping("/api/v1/account")
class AccountController(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder
) {
    @ApiOperation("Get the information about the actual authenticated user.")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun getMyAccount(@ApiIgnore principal: Principal) = AccountEntity(userRepository.findByUserName(principal.name).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.apply {
        Hibernate.initialize(groupMemberships)
    })

    @ApiOperation("Get the list of endpoints the actual authenticated user has access to.")
    @GetMapping("/endpoints")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    fun getMyEndpointPermissions(@ApiIgnore principal: Principal) = userRepository.findByUserName(principal.name).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.apply {
        Hibernate.initialize(permissions)
    }.permissions.map { it.endpointUUID to it.permission }.toMap()

    @ApiOperation("Change the actual authenticated user's password.")
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changeMyPassword(@RequestParam password: String, @ApiIgnore principal: Principal) {
        userRepository.findByUserName(principal.name).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            it.password = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }
}
