package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.dao.EmailAddress
import ch.hevs.cloudio.cloud.dao.UserRepository
import ch.hevs.cloudio.cloud.extension.userDetails
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.security.CloudioPermissionManager
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*


@RestController
@Profile("rest-api")
@Tag(name = "Account Management", description = "Allows users to access and modify their account information.")
@RequestMapping("/api/v1/account")
@SecurityRequirements(value = [
    SecurityRequirement(name = "basicAuth"),
    SecurityRequirement(name = "tokenAuth")
])
class AccountController(
    private val userRepository: UserRepository,
    private val permissionManager: CloudioPermissionManager,
    private val passwordEncoder: PasswordEncoder
) {
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    @Operation(summary = "Get information about the currently authenticated user.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Currently authenticated user's account.", responseCode = "200", content = [Content(schema = Schema(implementation = AccountEntity::class))]),
            ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getMyAccount(
        @Parameter(hidden = true) authentication: Authentication
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
    @Operation(summary = "Change the currently authenticated user's password.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Password was changed.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Existing password is incorrect.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putMyPassword(
        @RequestParam @Parameter(description = "Existing password.") existingPassword: String,
        @RequestParam @Parameter(description = "New password.") newPassword: String,
        @Parameter(hidden = true) authentication: Authentication
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
    @Operation(summary = "Returns the currently authenticated user's email address.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Users email address.", responseCode = "200", content = [Content(schema = Schema(type = "string", example = "john.doe@theinternet.org"))]),
            ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getMyEmailAddress(
        @Parameter(hidden = true) authentication: Authentication
    ) = userRepository.findById(authentication.userDetails().id).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.emailAddress.toString()

    @PutMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Changes the currently authenticated user's email address.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Email address was changed.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Invalid Email address.", responseCode = "400", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putMyEmailAddress(
        @Parameter(hidden = true) authentication: Authentication,
        @RequestParam @Parameter(description = "Email address to assign to user.", example = "john.doe@theinternet.org") email: String
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

    @GetMapping("/metaData", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns the currently authenticated user's meta data.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Users meta data.", responseCode = "200", content = [Content(schema = Schema(type = "object", example = "{\"location\": \"Sion\", \"position\": \"Manager\"}"))]),
            ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getMyMetaData(
        @Parameter(hidden = true) authentication: Authentication
    ) = userRepository.findById(authentication.userDetails().id).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    }.metaData

    @PutMapping("/metaData", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Changes the currently authenticated user's meta data.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Meta data was modified.", responseCode = "204", content = [Content()]),
            ApiResponse(description = "User not found.", responseCode = "404", content = [Content()]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun putMyMetaData(
        @Parameter(hidden = true) authentication: Authentication,
        @RequestBody @Parameter(description = "User's metadata.", schema = Schema(type = "object", example = "{\"location\": \"Sion\", \"position\": \"Manager\"}"))
        body: Map<String, Any>
    ) {
        userRepository.findById(authentication.userDetails().id).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            it.metaData = body.toMutableMap()
            userRepository.save(it)
        }
    }

    @GetMapping("/permissions", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    @Operation(summary = "Get the all endpoint permissions.")
    @ApiResponses(
        value = [
            ApiResponse(description = "Users endpoint permissions.", responseCode = "200", content = [Content(array = ArraySchema(schema = Schema(implementation = EndpointPermissionEntity::class)))]),
            ApiResponse(description = "Forbidden.", responseCode = "403", content = [Content()])
        ]
    )
    fun getMyEndpointPermissions(
        @Parameter(hidden = true) authentication: Authentication
    ) = permissionManager.resolvePermissions(authentication.userDetails()).map {
        EndpointPermissionEntity(
            endpoint = it.endpointUUID,
            permission = it.permission,
            modelPermissions = it.modelPermissions
        )
    }
}
