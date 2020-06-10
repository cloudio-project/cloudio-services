package ch.hevs.cloudio.cloud.restapi

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.restapi.account.AccountController
import ch.hevs.cloudio.cloud.security.Authority
import ch.hevs.cloudio.cloud.security.CloudioUserDetails
import ch.hevs.cloudio.cloud.security.CloudioUserDetailsService
import ch.hevs.cloudio.cloud.security.EndpointPermission
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class AccountControllerTest {
    @Autowired
    private lateinit var accountController: AccountController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var endpointRepository: EndpointRepository

    @Autowired
    lateinit var userDetailsService: CloudioUserDetailsService

    private var endpointUUID = UUID(0, 0)

    @Before
    fun setup() {
        userRepository.deleteAll()
        val user = userRepository.save(User(
                userName = "TestUser",
                emailAddress = EmailAddress("test.user@localhost"),
                password = passwordEncoder.encode("TestUserPassword"),
                authorities = mutableSetOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS),
                metaData = mutableMapOf("test" to 123)
        ))
        endpointRepository.deleteAll()
        val endpoint = endpointRepository.save(Endpoint(
                friendlyName = "TestEndpoint"
        ))
        endpointUUID = endpoint.uuid
        user.permissions.add(UserEndpointPermission(user.id, endpoint.uuid, EndpointPermission.READ))
        userRepository.save(user)
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getAccount() {
        val userDetails = userDetailsService.loadUserByUsername("TestUser") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        val account = accountController.getMyAccount(authentication)

        assert(account.name == "TestUser")
        assert(account.groupMemberships.isEmpty())
        assert(account.authorities == setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS))
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getAccountOfNonExistingUser() {
        val authentication = TestingAuthenticationToken(CloudioUserDetails(id = 2000,
                username = "TestUser2",
                password = "",
                authorities = listOf(SimpleGrantedAuthority(Authority.HTTP_ACCESS.toString())),
                banned = false,
                groupMembershipIDs = emptyList()
        ), null)

        assertThrows<CloudioHttpExceptions.NotFound> {
            accountController.getMyAccount(authentication)
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun changePassword() {
        val userDetails = userDetailsService.loadUserByUsername("TestUser") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        accountController.putMyPassword("MyNewPassword123777", authentication)

        val user = userRepository.findByUserName("TestUser").orElse(null)

        assert(user != null)
        assert(passwordEncoder.matches("MyNewPassword123777", user!!.password))
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun changePasswordOfNonExistingUser() {
        val authentication = TestingAuthenticationToken(CloudioUserDetails(id = 2000,
                username = "TestUser2",
                password = "",
                authorities = listOf(SimpleGrantedAuthority(Authority.HTTP_ACCESS.toString())),
                banned = false,
                groupMembershipIDs = emptyList()
        ), null)

        assertThrows<CloudioHttpExceptions.NotFound> {
            accountController.putMyPassword("MyNewPassword123777", authentication)
        }
    }
}
