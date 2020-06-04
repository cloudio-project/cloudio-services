package ch.hevs.cloudio.cloud.dao

import ch.hevs.cloudio.cloud.security.Authority
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@RunWith(SpringRunner::class)
@SpringBootTest
class UserRepositoryTests {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userRepository.deleteAll()
        userRepository.save(User(userName = "TestUser", emailAddress = EmailAddress("test.user@null.com")))
    }

    private fun transaction(block: () -> Unit) = transactionTemplate!!.executeWithoutResult {
        block()
    }

    @Test
    fun addMinimalUser() {
        transaction {
            userRepository.save(User(
                    userName = "MinimalUser",
                    emailAddress = EmailAddress("minimal.user@null.com"),
                    password = passwordEncoder.encode("MYPASS")
            ))
        }

        transaction {
            assert(userRepository.count() == 2L)
            val user = userRepository.findByUserName("MinimalUser").orElseThrow()
            assert(user.userName == "MinimalUser")
            assert(user.emailAddress.isValid())
            assert(user.emailAddress.address == "minimal.user@null.com")
            assert(passwordEncoder.matches("MYPASS", user.password))
            assert(user.authorities == setOf(Authority.HTTP_ACCESS))
            assert(!user.banned)
            assert(user.groupMemberships.isEmpty())
            assert(user.permissions.isEmpty())
            assert(user.metaData.isEmpty())
        }
    }

    @Test
    fun addCompleteUser() {
        transaction {
            userRepository.save(User(
                    userName = "CompleteUser",
                    emailAddress = EmailAddress("complete.user@null.com"),
                    password = passwordEncoder.encode("MYPASS"),
                    authorities = Authority.DEFAULT_AUTHORITIES.toMutableSet().apply {
                        add(Authority.BROKER_ACCESS)
                        add(Authority.HTTP_ADMIN)
                    },
                    banned = true,
                    metaData = mutableMapOf("nickname" to "minx", "age" to 42, "inHouse" to true, "toto" to mapOf("a" to 0, "b" to 1))
            ))
        }

        transaction {
            assert(userRepository.count() == 2L)
            val user = userRepository.findByUserName("CompleteUser").orElseThrow()
            assert(user.userName == "CompleteUser")
            assert(user.emailAddress.isValid())
            assert(user.emailAddress.address == "complete.user@null.com")
            assert(passwordEncoder.matches("MYPASS", user.password))
            assert(user.authorities == setOf(Authority.HTTP_ACCESS, Authority.BROKER_ACCESS, Authority.HTTP_ADMIN))
            assert(user.banned)
            assert(user.groupMemberships.isEmpty())
            assert(user.permissions.isEmpty())
            assert(user.metaData.isNotEmpty())
            assert(user.metaData["nickname"] == "minx")
            assert(user.metaData["age"] == 42)
            assert(user.metaData["inHouse"] == true)
            assert(user.metaData["toto"] == mapOf("a" to 0, "b" to 1))
        }
    }

    @Test
    fun addExistingUser() {
        assertThrows<DataIntegrityViolationException> {
            userRepository.save(User(
                    userName = "TestUser"
            ))
        }
    }

    @Test
    fun modifyUser() {
        transaction {
            val user = userRepository.findByUserName("TestUser").orElseThrow()
            user.banned = true
            userRepository.save(user)
        }

        transaction {
            assert(userRepository.count() == 1L)
            val user = userRepository.findByUserName("TestUser").orElseThrow()
            assert(user.userName == "TestUser")
            assert(user.emailAddress.isValid())
            assert(user.emailAddress.address == "test.user@null.com")
            assert(user.password.isEmpty())
            assert(user.authorities == setOf(Authority.HTTP_ACCESS))
            assert(user.banned)
            assert(user.groupMemberships.isEmpty())
            assert(user.permissions.isEmpty())
            assert(user.metaData.isEmpty())
        }
    }

    @Test
    fun deleteUser() {
        transaction {
            userRepository.deleteByUserName("TestUser")
        }

        transaction {
            assert(userRepository.count() == 0L)
        }
    }
}
