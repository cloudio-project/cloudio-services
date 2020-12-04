package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.dao.EmailAddress
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import io.swagger.annotations.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

@Api(
        tags = ["Account"],
        description = "Allows users to access and modify their account information."
)
@RestController
@RequestMapping("/api/v1/account")
class AccountController(
        private val userRepository: UserRepository,
        private val permissionManager: CloudioPermissionManager,
        private val passwordEncoder: PasswordEncoder
) {
    @GetMapping("", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    @ApiOperation("Get information about the currently authenticated user.")
    fun getMyAccount(
            @ApiIgnore authentication: Authentication
    ) = userRepository.findById(authentication.userDetails().id).orElseThrow {
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

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Change the currently authenticated user's password.")
    fun putMyPassword(
            @RequestParam @ApiParam("Existing password.") existingPassword: String,
            @RequestParam @ApiParam("New password.") newPassword: String,
            @ApiIgnore authentication: Authentication
    ) {
        userRepository.findById(authentication.userDetails().id).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            if (!passwordEncoder.matches(existingPassword, it.password)) {
                throw CloudioHttpExceptions.BadRequest("Existing password is incorrect.")
            }
            it.password = passwordEncoder.encode(newPassword)
            userRepository.save(it)
        }
    }

    @GetMapping("/email", produces = ["text/plain"])
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Returns the currently authenticated user's email address.")
    // TODO: Response example.
    fun getMyEmailAddress(
            @ApiIgnore authentication: Authentication
    ) = userRepository.findById(authentication.userDetails().id).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.emailAddress.toString()

    @PutMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Changes the currently authenticated user's email address.")
    fun putMyEmailAddress(
            @ApiIgnore authentication: Authentication,
            @RequestParam @ApiParam("Email address to assign to user.") email: String
    ) {
        userRepository.findById(authentication.userDetails().id).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            val emailAddress = EmailAddress(email)
            if (!emailAddress.isValid()) {
                throw CloudioHttpExceptions.BadRequest("Invalid Email address.")
            }
            it.emailAddress = emailAddress
            userRepository.save(it)
        }
    }

    @GetMapping("/metaData", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Returns the currently authenticated user's meta data.")
    fun getMyMetaData(
            @ApiIgnore authentication: Authentication
    ) = userRepository.findById(authentication.userDetails().id).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.metaData

    @PutMapping("/metaData")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Changes the currently authenticated user's meta data.")
    fun putMyMetaData(
            @ApiIgnore authentication: Authentication,
            @RequestBody @ApiParam("User's metadata.") body: Map<String, Any>
    ) {
        userRepository.findById(authentication.userDetails().id).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            it.metaData = body.toMutableMap()
            userRepository.save(it)
        }
    }

    @GetMapping("/permissions", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    @ApiOperation("Get the all endpoint permissions.")
    fun getMyEndpointPermissions(
            @ApiIgnore authentication: Authentication
    ) = permissionManager.resolvePermissions(authentication.userDetails()).map {
        EndpointPermissionEntity(
                endpoint = it.endpointUUID,
                permission = it.permission,
                modelPermissions = it.modelPermissions
        )
    }

    // TODO: Add grant operation.
}
