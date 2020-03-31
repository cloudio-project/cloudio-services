package ch.hevs.cloudio.cloud.restapi.account

import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.Permission
import ch.hevs.cloudio.cloud.security.PermissionPriority
import ch.hevs.cloudio.cloud.security.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import java.security.Principal

@RunWith(SpringRunner::class)
@SpringBootTest
class AccountControllerTest {
    @Autowired
    private lateinit var accountController: AccountController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Before
    fun setup() {
        userRepository.deleteAll()
        userRepository.save(User(
                userName = "TestUser",
                passwordHash = passwordEncoder.encode("TestUserPassword"),
                authorities = setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                userGroups = setOf(),
                permissions = mutableMapOf("#/toto" to PrioritizedPermission(Permission.READ, PermissionPriority.HIGH)),
                banned = false
        ))
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getAccount() {
        val account = accountController.getAccount(Principal { "TestUser" })

        assert(account.name == "TestUser")
        assert(account.permissions.count() == 1 && account.permissions["#/toto"] == PrioritizedPermission(Permission.READ, PermissionPriority.HIGH))
        assert(account.groupMemberships.isEmpty())
        assert(account.authorities == setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS))
    }

    @Test
    @WithMockUser("TestUser2", authorities = ["HTTP_ACCESS"])
    fun getAccountOfNonExistingUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            accountController.getAccount(Principal { "TestUser2" })
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun changePassword() {
        accountController.putAccountPassword("MyNewPassword123777", Principal { "TestUser" })

        val user = userRepository.findByIdOrNull("TestUser")

        assert(user != null)
        assert(passwordEncoder.matches("MyNewPassword123777", user!!.passwordHash))
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun changePasswordOfNonExistingUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            accountController.putAccountPassword("MyNewPassword123777", Principal { "TestUser2" })
        }
    }
}