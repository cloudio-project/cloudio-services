package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertFails

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserManagementUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var userName: String

    @BeforeAll
    fun setup() {

        userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)
        UserManagementUtil.createUser(userRepository, userTest)
    }

    @AfterAll
    fun cleanUp() {
        UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
    }

    @Test
    fun createDeleteUser() {
        val createUserName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(createUserName)
        UserManagementUtil.createUser(userRepository, userTest)
        UserManagementUtil.deleteUser(userRepository, UserRequest(createUserName))
        //user shouldn't exist
        val user = UserManagementUtil.getUser(userRepository, UserRequest(createUserName))
        assert(user == null)
        //user shouldn't be in the list
        val list = UserManagementUtil.getUserList(userRepository)
        assert(!list.userList.contains(createUserName))
    }

    @Test
    fun getUserList() {
        //test that user is in the user list
        val list = UserManagementUtil.getUserList(userRepository)
        assert(list.userList.contains(userName))
    }

    @Test
    fun getUser() {
        var user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
        assert(user != null)
        assert(user?.userName == userName)
    }

    @Test
    fun modifyUserPassword() {
        //modify user password
        UserManagementUtil.modifyUserPassword(userRepository, UserPasswordRequest(userName, "password"))
        //test new password
        assert(UserManagementUtil.getUser(userRepository, UserRequest(userName))?.passwordHash == "password")
    }

    @Test
    fun addUserAuthority() {
        //add authority
        UserManagementUtil.addUserAuthority(userRepository, AddAuthorityRequest(userName, setOf(Authority.BROKER_MANAGEMENT_POLICYMAKER)))
        //test authority
        assert(UserManagementUtil.getUser(userRepository, UserRequest(userName))?.authorities?.contains(Authority.BROKER_MANAGEMENT_POLICYMAKER) == true)
    }

    @Test
    fun removeUserAuthority() {
        //add authority
        UserManagementUtil.addUserAuthority(userRepository, AddAuthorityRequest(userName, setOf(Authority.BROKER_MANAGEMENT_ADMINISTRATOR)))

        //remove authority
        UserManagementUtil.removeUserAuthority(userRepository, RemoveAuthorityRequest(userName, Authority.BROKER_MANAGEMENT_ADMINISTRATOR))
        //test authority is removed
        assert((UserManagementUtil.getUser(userRepository, UserRequest(userName))?.authorities?.contains(Authority.BROKER_MANAGEMENT_ADMINISTRATOR)) == false)
    }

    @Test
    fun randomCharacterUserTest() {
        //test that the random user cannot be retreived
        assert(UserManagementUtil.getUser(userRepository, UserRequest(randomCharacters)) == null)
        assertFails { UserManagementUtil.modifyUserPassword(userRepository, UserPasswordRequest(randomCharacters, "password")) }
        assertFails { UserManagementUtil.addUserAuthority(userRepository, AddAuthorityRequest(randomCharacters, setOf(Authority.BROKER_MANAGEMENT_POLICYMAKER))) }
        assertFails { UserManagementUtil.removeUserAuthority(userRepository, RemoveAuthorityRequest(randomCharacters, Authority.BROKER_MANAGEMENT_POLICYMAKER)) }
        assertFails { UserManagementUtil.deleteUser(userRepository, UserRequest(randomCharacters)) }
    }
}