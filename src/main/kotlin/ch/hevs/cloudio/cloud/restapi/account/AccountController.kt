package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@Api(tags = ["Account"], description = "Allows user to interact with his own account.")
@RestController
@RequestMapping("/api/v1/account")
class AccountController(
        private val userRepository: UserRepository,
        private val permissionManager: CloudioPermissionManager,
        private val passwordEncoder: PasswordEncoder
) {
    @ApiOperation("Get the information about the actual authenticated user.")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    fun getMyAccount(@ApiIgnore authentication: Authentication) = userRepository.findById(authentication.userDetails().id).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.run {
        AccountEntity(
                name = userName,
                email = emailAddress.toString(),
                authorities = authorities,
                groupMemberships = groupMemberships.map { it.groupName },
                metadata = metaData
        )
    }

    @ApiOperation("Get the all endpoint permissions.")
    @GetMapping("/permissions")
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    fun getMyEndpointPermissions(@ApiIgnore authentication: Authentication) = permissionManager.resolvePermissions(authentication.userDetails())

    @ApiOperation("Change the actual authenticated user's password.")
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changeMyPassword(@RequestParam password: String, @ApiIgnore authentication: Authentication) {
        userRepository.findById(authentication.userDetails().id).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            it.password = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }
}
