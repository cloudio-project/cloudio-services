package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.model.ModelIdentifier
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class PermissionManagerTests {
    @Autowired
    lateinit var permissionManager: CloudioPermissionManager

    @Autowired
    lateinit var userDetailsService: CloudioUserDetailsService

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    lateinit var endpointRepository: EndpointRepository

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    private lateinit var testEndpointUUID: UUID

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userRepository.deleteAll()
        userGroupRepository.deleteAll()
        endpointRepository.deleteAll()
        testEndpointUUID = endpointRepository.save(Endpoint(
                friendlyName = "MyEndpoint1"
        )).uuid
    }

    private fun <R> transaction(block: () -> R): R = transactionTemplate!!.execute {
        return@execute block()
    }!!

    @Test
    fun invalidEndpointUUID() {
        transaction {
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.ACCESS))
        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, "4578", EndpointPermission.OWN))
    }

    @Test
    fun userDenyEndpointPermission() {
        transaction {
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userAccessEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.ACCESS))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userBrowseEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.BROWSE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userReadEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.READ))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userWriteEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.WRITE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userConfigureEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.CONFIGURE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userGrantEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.GRANT))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userOwnEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.OWN))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupAccessEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.ACCESS))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupBrowseEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.BROWSE))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupReadEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.READ))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupWriteEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.WRITE))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupConfigureEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.CONFIGURE))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupGrantEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.GRANT))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun groupOwnEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpointUUID, EndpointPermission.OWN))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpointUUID.toString(), EndpointPermission.OWN))
    }

    @Test
    fun userDenyEndpointDenyEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
        assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
        assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

        assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
        assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
        assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

        assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
    }

    @Test
    fun invalidEndpointModelElementIdentifier() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.OWN))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(!permissionManager.hasPermission(authentication, "32589797345/3434", EndpointModelElementPermission.VIEW))
        assert(!permissionManager.hasPermission(authentication, "32589797345/3434", EndpointModelElementPermission.READ))
        assert(!permissionManager.hasPermission(authentication, "32589797345/3434", EndpointModelElementPermission.WRITE))

        assert(!permissionManager.hasPermission(authentication, "${testEndpointUUID}4", EndpointModelElementPermission.VIEW))
        assert(!permissionManager.hasPermission(authentication, "${testEndpointUUID}4", EndpointModelElementPermission.READ))
        assert(!permissionManager.hasPermission(authentication, "${testEndpointUUID}4", EndpointModelElementPermission.WRITE))

        assert(!permissionManager.hasPermission(authentication, "${testEndpointUUID}//node", EndpointModelElementPermission.VIEW))
        assert(!permissionManager.hasPermission(authentication, "${testEndpointUUID}//node", EndpointModelElementPermission.READ))
        assert(!permissionManager.hasPermission(authentication, "${testEndpointUUID}//node", EndpointModelElementPermission.WRITE))
    }

    @Test
    fun userAccessEndpointDenyEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.ACCESS))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userAccessEndpointViewEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.ACCESS)
            permission.modelPermissions[modelIdentifier.modelPath()] = EndpointModelElementPermission.VIEW
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userBrowseEndpointViewEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.BROWSE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userAccessEndpointReadEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.ACCESS)
            permission.modelPermissions[modelIdentifier.modelPath()] = EndpointModelElementPermission.READ
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userBrowseEndpointReadEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.BROWSE)
            permission.modelPermissions[modelIdentifier.modelPath()] = EndpointModelElementPermission.READ
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userReadEndpointReadEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.READ)
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(!permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(!permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userAccessEndpointWriteEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.ACCESS)
            permission.modelPermissions[modelIdentifier.modelPath()] = EndpointModelElementPermission.WRITE
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userBrowseEndpointWriteEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.BROWSE)
            permission.modelPermissions[modelIdentifier.modelPath()] = EndpointModelElementPermission.WRITE
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userReadEndpointWriteEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.READ)
            permission.modelPermissions[modelIdentifier.modelPath()] = EndpointModelElementPermission.WRITE
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }

    @Test
    fun userWriteEndpointWriteEndpointModelElementPermission() {
        val modelIdentifier = ModelIdentifier("$testEndpointUUID.n1.o1.a1")
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            val permission = UserEndpointPermission(user.id, testEndpointUUID, EndpointPermission.WRITE)
            user.permissions.add(permission)
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        transaction {
            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.VIEW))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.VIEW))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.READ))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.READ))

            assert(permissionManager.hasEndpointModelElementPermission(userDetails, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier, EndpointModelElementPermission.WRITE))
            assert(permissionManager.hasPermission(authentication, modelIdentifier.toString(), EndpointModelElementPermission.WRITE))
        }
    }
}
