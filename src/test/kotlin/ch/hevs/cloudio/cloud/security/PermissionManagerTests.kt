package ch.hevs.cloudio.cloud.security

import ch.hevs.cloudio.cloud.dao.*
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

    private lateinit var testEndpoint1UUID: UUID
    private lateinit var testEndpoint2UUID: UUID

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userRepository.deleteAll()
        userGroupRepository.deleteAll()
        endpointRepository.deleteAll()
        testEndpoint1UUID = endpointRepository.save(Endpoint(
                friendlyName = "MyEndpoint1"
        )).uuid
        testEndpoint2UUID = endpointRepository.save(Endpoint(
                friendlyName = "MyEndpoint2"
        )).uuid
    }

    private fun <R> transaction(block: () -> R): R = transactionTemplate!!.execute {
        return@execute block()
    }!!

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

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userAccessEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.ACCESS))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userBrowseEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.BROWSE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userReadEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.READ))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userWriteEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.WRITE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userConfigureEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.CONFIGURE))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userGrantEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.GRANT))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun userOwnEndpointPermission() {
        transaction {
            val user = userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
            user.permissions.add(UserEndpointPermission(user.id, testEndpoint1UUID, EndpointPermission.OWN))
            userRepository.save(user)
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupAccessEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.ACCESS))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupBrowseEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.BROWSE))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupReadEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.READ))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupWriteEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.WRITE))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupConfigureEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.CONFIGURE))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupGrantEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.GRANT))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(!permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(!permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }

    @Test
    fun groupOwnEndpointPermission() {
        transaction {
            val group = userGroupRepository.save(UserGroup("TestGroup"))
            group.permissions.add(UserGroupEndpointPermission(group.id, testEndpoint1UUID, EndpointPermission.OWN))
            userRepository.save(User(
                    userName = "Test",
                    emailAddress = EmailAddress("test@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    groupMemberships = mutableSetOf(group)
            ))
        }

        val userDetails = userDetailsService.loadUserByUsername("Test") as CloudioUserDetails
        val authentication = TestingAuthenticationToken(userDetails, null)

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.ACCESS))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.ACCESS))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.BROWSE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.BROWSE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.READ))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.READ))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.WRITE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.WRITE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.CONFIGURE))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.CONFIGURE))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.GRANT))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.GRANT))

        assert(permissionManager.hasEndpointPermission(userDetails, testEndpoint1UUID, EndpointPermission.OWN))
        assert(permissionManager.hasPermission(authentication, testEndpoint1UUID, EndpointPermission.OWN))
    }
}
