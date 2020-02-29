package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.CloudioHttpExceptions
import ch.hevs.cloudio.cloud.restapi.admin.user.UserManagementController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertFails

@RunWith(SpringRunner::class)
@SpringBootTest
class UserManagementUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var userManagement: UserManagementController

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var userName: String

    @Before
    fun setup() {
        userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser()
        userManagement.postUserByUserName(userName, userTest)
    }

    @After
    fun cleanUp() {
        userManagement.deleteUser(userName)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createDeleteUser() {
        val createUserName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser()
        userManagement.postUserByUserName(createUserName, userTest)
        userManagement.deleteUser(createUserName)
        //user shouldn't exist
        try {
            userManagement.getUserByUserName(createUserName)
            assert(false)
        } catch (exception: CloudioHttpExceptions.NotFound) { }
        //user shouldn't be in the list
        val list = userManagement.getUsers()
        assert(list.none { it.userName == createUserName })
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUserList() {
        //test that user is in the user list
        val list = userManagement.getUsers()
        assert(!list.none { it.userName == userName })
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUser() {
        userManagement.getUserByUserName(userName)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun modifyUserPassword() {
        //modify user password
        userManagement.putUserPassword(userName, "password")
        //test new password
        assert(passwordEncoder.matches("password", userRepository.findById(userName).get().passwordHash))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun addUserAuthority() {
        // TODO
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun removeUserAuthority() {
        // TODO
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun randomCharacterUserTest() {
        //test that the random user cannot be retreived

        // TODO:
        //assert(userManagement.getUserByUserName(randomCharacters))
        //assertFails { UserManagementUtil.modifyUserPassword(userRepository, UserPasswordRequest(randomCharacters, "password")) }
        //assertFails { UserManagementUtil.addUserAuthority(userRepository, AddAuthorityRequest(randomCharacters, setOf(Authority.BROKER_MANAGEMENT_POLICYMAKER))) }
        //assertFails { UserManagementUtil.removeUserAuthority(userRepository, RemoveAuthorityRequest(randomCharacters, Authority.BROKER_MANAGEMENT_POLICYMAKER)) }
        //assertFails { UserManagementUtil.deleteUser(userRepository, UserRequest(randomCharacters)) }
    }
}