package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.repo.authentication.MONGOUserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.security.Principal

@Api(tags = ["Account"], description = "Allows user to interact with his own account.")
@RestController
@RequestMapping("/api/v1/account")
class AccountController(
        private val userRepository: MONGOUserRepository,
        private val passwordEncoder: PasswordEncoder
) {
    @ApiOperation("Get the information about the actual authenticated user.")
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getMyAccount(@ApiIgnore principal: Principal) = AccountEntity(userRepository.findById(principal.name).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    })

    @ApiOperation("Change the actual authenticated user's password.")
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changeMyPassword(@RequestParam password: String, @ApiIgnore principal: Principal) {
        userRepository.findById(principal.name).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            it.passwordHash = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }
}
