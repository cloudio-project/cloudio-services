package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder
) {
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    fun getAccount(principal: Principal) = AccountBody(userRepository.findById(principal.name).orElseThrow {
        CloudioHttpExceptions.NotFound("User not found.")
    })

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun putAccountPassword(@RequestParam password: String, principal: Principal) {
        userRepository.findById(principal.name).orElseThrow {
            CloudioHttpExceptions.NotFound("User not found.")
        }.let {
            it.passwordHash = passwordEncoder.encode(password)
            userRepository.save(it)
        }
    }
}
