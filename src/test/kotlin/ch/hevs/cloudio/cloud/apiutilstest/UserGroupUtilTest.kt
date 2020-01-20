package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
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
class UserGroupUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var userName: String
    private lateinit var userGroupName: String

    @BeforeAll
    fun setup() {
        userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)

        userGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName, emptySet())

        //create an User
        UserManagementUtil.createUser(userRepository, userTest)

        //create an User Group
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)
    }

    @AfterAll
    fun cleanUp() {
        //remove users and user group
        UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))
    }

    @Test
    fun createUserGroup() {
        val createUserGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(createUserGroupName, emptySet())
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)

        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(createUserGroupName))
    }

    @Test
    fun getUserGroup() {
        //test that we can retrieve user group
        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        assert(userGroup != null)
        assert(userGroup?.userGroupName == userGroupName)

    }

    @Test
    fun getUserGroupList() {
        //test that user is in the user list
        val list = UserGroupUtil.getUserGroupList(userGroupRepository)
        assert(list.userGroupList.contains(userGroupName))
    }

    @Test
    fun addUserToGroup() {
        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName, setOf(userName)))

        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        val user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
        //test that user is part of usergroup and vice-versa
        assert(userGroup!!.usersList.contains(userName))
        assert(user!!.userGroups.contains(userGroupName))
    }

    @Test
    fun deleteUserToGroup() {
        val userName2 = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName2)
        UserManagementUtil.createUser(userRepository, userTest)

        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName, setOf(userName2)))
        //remove user from user group
        UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, UserGroupUserRequest(userGroupName, userName2))

        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        val user = UserManagementUtil.getUser(userRepository, UserRequest(userName2))
        //test that user is not part of usergroup and vice-versa
        assert(!userGroup!!.usersList.contains(userName2))
        assert(!user!!.userGroups.contains(userGroupName))
    }

    @Test
    fun deleteUserGroup() {

        val userName2 = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName2)
        UserManagementUtil.createUser(userRepository, userTest)

        val userGroupName2 = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName2, emptySet())
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)

        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName2, setOf(userName2)))
        //delete user group
        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName2))

        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName2))
        val user = UserManagementUtil.getUser(userRepository, UserRequest(userName2))
        //test that user is not part of usergroup and that user group doesn't exist
        assert(userGroup == null)
        assert(!user!!.userGroups.contains(userGroupName2))

        //user group shouldn't be in the list
        val list = UserGroupUtil.getUserGroupList(userGroupRepository)
        assert(!list.userGroupList.contains(userGroupName2))
    }

    @Test
    fun randomCharacterUserGroupTest() {
        //test that we can retrieve random name user group
        assert(UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(randomCharacters)) == null)
        assertFails { UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(randomCharacters, setOf(userName))) }
        assertFails { UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, UserGroupUserRequest(randomCharacters, userName)) }
        assertFails { UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(randomCharacters)) }
    }
}