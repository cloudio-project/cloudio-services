package ch.hevs.cloudio.cloud.restapi.admin

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.dao.*
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.admin.user.PostUserEntity
import ch.hevs.cloudio.cloud.restapi.admin.user.UserEntity
import ch.hevs.cloudio.cloud.restapi.admin.user.UserManagementController
import ch.hevs.cloudio.cloud.security.Authority
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@RunWith(SpringRunner::class)
@SpringBootTest
class UserManagementControllerTests {
    @Autowired
    private lateinit var userManagementController: UserManagementController

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var groupRepository: UserGroupRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var transactionManager: AbstractPlatformTransactionManager
    private var transactionTemplate: TransactionTemplate? = null

    @Before
    fun setup() {
        transactionTemplate = TransactionTemplate(transactionManager)
        userRepository.deleteAll()
        groupRepository.deleteAll()
        val testGroup = groupRepository.save(UserGroup(
                groupName = "TestGroup"
        ))
        userRepository.save(User(
                userName = "TestUser",
                emailAddress = EmailAddress("no@thing.com"),
                password = passwordEncoder.encode("TestUserPassword"),
                authorities = Authority.DEFAULT_AUTHORITIES.toMutableSet(),
                groupMemberships = mutableSetOf(testGroup),
                permissions = mutableSetOf(),
                banned = false
        ))
    }

    private fun <R> transaction(block: () -> R): R = transactionTemplate!!.execute {
        return@execute block()
    }!!

    // Create user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createAndDeleteMinimalUser() {
        userManagementController.postUser(PostUserEntity(
                name = "User1",
                email = "no@thing.com",
                password = "88888888"
        ))

        transaction {
            val user = userRepository.findByUserName("User1").orElse(null)
            assert(user != null)
            user?.apply {
                assert(userName == "User1")
                assert(emailAddress.toString() == "no@thing.com")
                assert(passwordEncoder.matches("88888888", password))
                assert(authorities == Authority.DEFAULT_AUTHORITIES)
                assert(!banned)
                assert(groupMemberships.isEmpty())
                assert(permissions.isEmpty())
                assert(metaData.isEmpty())
            }
        }

        userManagementController.deleteUserByUserName("User1")
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.deleteUserByUserName("User1")
        }

        assert(userRepository.findByUserName("User1").isEmpty)
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createAndDeleteCompleteUser() {
        userManagementController.postUser(PostUserEntity(
                name = "User1",
                email = "no@thing.com",
                password = "tototititata",
                authorities = setOf(Authority.HTTP_ACCESS),
                banned = false,
                groupMemberships = setOf("TestGroup"),
                metaData = mapOf("test" to true, "toto" to 555)
        ))

        transaction {
            val user = userRepository.findByUserName("User1").orElse(null)
            assert(user != null)
            user?.apply {
                assert(userName == "User1")
                assert(emailAddress.toString() == "no@thing.com")
                assert(passwordEncoder.matches("tototititata", password))
                assert(authorities == setOf(Authority.HTTP_ACCESS))
                assert(!banned)
                assert(groupMemberships.count() == 1 && groupMemberships.first().groupName == "TestGroup")
                assert(permissions.isEmpty())
                assert(metaData.count() == 2 && metaData["test"] == true && metaData["toto"] == 555)
            }
        }

        userManagementController.deleteUserByUserName("User1")
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.deleteUserByUserName("User1")
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun recreateExistingUser() {
        assertThrows<CloudioHttpExceptions.Conflict> {
            userManagementController.postUser(PostUserEntity(
                    name = "TestUser",
                    email = "no@thing.com",
                    password = "88888888"
            ))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserWithInvalidEmail() {
        assertThrows<CloudioHttpExceptions.BadRequest> {
            userManagementController.postUser(PostUserEntity(
                    name = "TestUser2",
                    email = "nothing.com",
                    password = "88888888"
            ))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserInNonExistingGroup() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.postUser(PostUserEntity(
                    name = "User1",
                    email = "no@thing.com",
                    password = "88888888",
                    groupMemberships = setOf("NonexistingGroup")
            ))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserInExistingGroup() {
        userManagementController.postUser(PostUserEntity(
                name = "User1",
                email = "no@thing.com",
                password = "88888888",
                groupMemberships = setOf("TestGroup")
        ))

        transaction {
            val user = userRepository.findByUserName("User1").orElse(null)
            assert(user != null)
            user?.apply {
                assert(userName == "User1")
                assert(emailAddress.toString() == "no@thing.com")
                assert(passwordEncoder.matches("88888888", password))
                assert(authorities == Authority.DEFAULT_AUTHORITIES)
                assert(!banned)
                assert(groupMemberships.count() == 1 && groupMemberships.first().groupName == "TestGroup")
                assert(permissions.isEmpty())
                assert(metaData.isEmpty())
            }
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createAdminUser() {
        userManagementController.postUser(PostUserEntity(
                name = "Admin1",
                email = "no@thing.com",
                password = "1234567812345678",
                authorities = Authority.ALL_AUTHORITIES
        ))

        transaction {
            val user = userRepository.findByUserName("Admin1").orElse(null)
            assert(user != null)
            user?.apply {
                assert(userName == "Admin1")
                assert(emailAddress.toString() == "no@thing.com")
                assert(passwordEncoder.matches("1234567812345678", password))
                assert(authorities == Authority.ALL_AUTHORITIES)
                assert(!banned)
                assert(groupMemberships.isEmpty())
                assert(permissions.isEmpty())
                assert(metaData.isEmpty())
            }
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun createUserNotByAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.postUser(PostUserEntity(
                    name = "User1",
                    email = "no@thing.com",
                    password = "88888888"
            ))
        }
    }

    // Get user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUser() {
        val user = userManagementController.getUserByUserName("TestUser")
        assert(user.name == "TestUser")
        assert(user.email == "no@thing.com")
        assert(user.authorities == Authority.DEFAULT_AUTHORITIES)
        assert(!user.banned)
        assert(user.groupMemberships.count() == 1 && user.groupMemberships.first() == "TestGroup")
        assert(user.metadata.isEmpty())
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getNonExistingUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.getUserByUserName("TestUserNotExist")
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getUserByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.getUserByUserName("TestUser")
        }
    }

    // Delete user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteUser() {
        userManagementController.deleteUserByUserName("TestUser")
        assert(userRepository.count() == 0L)
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun deleteUserNonByAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.deleteUserByUserName("User1")
        }
    }

    // Modify user

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUser() {
        userManagementController.getUserByUserName("TestUser").let {
            it.email = "an@other.com"
            it.authorities = setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN)
            it.banned = true
            it.groupMemberships = emptySet()
            it.metadata = mapOf("hans" to "wurst")
            userManagementController.updateUserByUserName("TestUser", it)
        }

        transaction {
            userRepository.findByUserName("TestUser").orElseThrow().apply {
                assert(userName == "TestUser")
                assert(emailAddress.toString() == "an@other.com")
                assert(authorities == setOf(Authority.HTTP_ACCESS, Authority.HTTP_ADMIN))
                assert(banned)
                assert(groupMemberships.isEmpty())
                assert(permissions.isEmpty())
            }
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserAddNonExistingGroup() {
        userManagementController.getUserByUserName("TestUser").let {
            it.groupMemberships = setOf("NonExistingGroup")
            assertThrows<CloudioHttpExceptions.NotFound> {
                userManagementController.updateUserByUserName("TestUser", it)
            }
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserAddExistingGroup() {
        transaction {
            userRepository.findByUserName("TestUser").orElseThrow().let {
                it.groupMemberships.clear()
                userRepository.save(it)
            }
            userManagementController.getUserByUserName("TestUser").let {
                it.groupMemberships = setOf("TestGroup")
                userManagementController.updateUserByUserName("TestUser", it)
            }
            userRepository.findByUserName("TestUser").orElseThrow().apply {
                assert(groupMemberships.count() == 1 && groupMemberships.first().groupName == "TestGroup")
            }
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyNonExistentUser() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.updateUserByUserName("ThisDoesNotExist",
                    UserEntity("ThisDoesNotExist", "no@thing.com", emptySet(), false, emptySet(), emptyMap()))
        }
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserWhereUserNamesDoNotMatch() {
        assertThrows<CloudioHttpExceptions.BadRequest> {
            userManagementController.updateUserByUserName("TestUser",
                    UserEntity("ThestUser", "no@thing.com", emptySet(), false, emptySet(), emptyMap()))
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun modifyUserByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.updateUserByUserName("ThisDoesNotExist",
                    UserEntity("ThisDoesNotExist", "no@thing.com", emptySet(), false, emptySet(), emptyMap()))
        }
    }

    // Change password

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun changeUsersPassword() {
        userManagementController.changeUserPassword("TestUser", "toto")
        assert(passwordEncoder.matches("toto", userRepository.findByUserName("TestUser").orElseThrow().password))
    }

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun changeNonExistentUsersPassword() {
        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.changeUserPassword("TestUserThatDoesNotExist", "toto")
        }
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun changeUsersPasswordByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.changeUserPassword("TestUser", "toto")
        }
        assert(passwordEncoder.matches("TestUserPassword", userRepository.findByUserName("TestUser").orElseThrow().password))
    }

    // Get all users

    @Test
    @WithMockUser("admin", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getAllUsers() {
        val users = userManagementController.getAllUsers()
        assert(users.count() == 1)
        assert(users.first().let { it.name == "TestUser" && it.email == "no@thing.com" && it.authorities == Authority.DEFAULT_AUTHORITIES && !it.banned })
    }

    @Test
    @WithMockUser("TestUser", authorities = ["HTTP_ACCESS"])
    fun getAllUsersByNonAdmin() {
        assertThrows<AccessDeniedException> {
            userManagementController.getAllUsers()
        }
    }

    // Random user name tests

    @Test
    @WithMockUser(username = "root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun randomCharacterUserTest() {
        val randomCharacters = TestUtil.generateRandomString(15)

        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.getUserByUserName(randomCharacters)
        }

        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.changeUserPassword(randomCharacters, "12345678900")
        }

        assertThrows<CloudioHttpExceptions.NotFound> {
            val user = userManagementController.getUserByUserName("TestUser")
            user.name = randomCharacters
            userManagementController.updateUserByUserName(randomCharacters, user)
        }

        assertThrows<CloudioHttpExceptions.NotFound> {
            userManagementController.deleteUserByUserName(randomCharacters)
        }
    }
}
