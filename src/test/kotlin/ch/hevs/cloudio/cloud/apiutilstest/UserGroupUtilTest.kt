package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.admin.user.PostUserBody
import ch.hevs.cloudio.cloud.restapi.admin.user.UserManagementController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertFails

@RunWith(SpringRunner::class)
@SpringBootTest
class UserGroupUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var userManagement: UserManagementController

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var userName: String
    private lateinit var userGroupName: String

    @Before
    fun setup() {
        userName = TestUtil.generateRandomString(15)

        userGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName, emptySet())

        //create an User
        userManagement.postUserByUserName(userName, PostUserBody(password = "test"))

        //create an User Group
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)
    }

    @After
    fun cleanUp() {
        //remove users and user group
        userManagement.deleteUser(userName)
        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserGroup() {
        val createUserGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(createUserGroupName, emptySet())
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)

        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(createUserGroupName))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUserGroup() {
        //test that we can retrieve user group
        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        assert(userGroup != null)
        assert(userGroup?.userGroupName == userGroupName)

    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUserGroupList() {
        //test that user is in the user list
        val list = UserGroupUtil.getUserGroupList(userGroupRepository)
        assert(list.userGroupList.contains(userGroupName))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun addUserToGroup() {
        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName, setOf(userName)))

        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        val user = userManagement.getUserByUserName(userName)
        //test that user is part of usergroup and vice-versa
        assert(userGroup!!.usersList.contains(userName))
        assert(user.groupMemberships.contains(userGroupName))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteUserToGroup() {
        val userName2 = TestUtil.generateRandomString(15)
        userManagement.postUserByUserName(userName2, PostUserBody(password = "test"))

        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName, setOf(userName2)))
        //remove user from user group
        UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, UserGroupUserRequest(userGroupName, userName2))

        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        val user = userManagement.getUserByUserName(userName2)
        //test that user is not part of usergroup and vice-versa
        assert(!userGroup!!.usersList.contains(userName2))
        assert(!user.groupMemberships.contains(userGroupName))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun deleteUserGroup() {

        val userName2 = TestUtil.generateRandomString(15)
        userManagement.postUserByUserName(userName2, PostUserBody(password = "test"))

        val userGroupName2 = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName2, emptySet())
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)

        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName2, setOf(userName2)))
        //delete user group
        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName2))

        val userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName2))
        val user = userManagement.getUserByUserName(userName2)
        //test that user is not part of usergroup and that user group doesn't exist
        assert(userGroup == null)
        assert(!user.groupMemberships.contains(userGroupName2))

        //user group shouldn't be in the list
        val list = UserGroupUtil.getUserGroupList(userGroupRepository)
        assert(!list.userGroupList.contains(userGroupName2))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun randomCharacterUserGroupTest() {
        //test that we can retrieve random name user group
        assert(UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(randomCharacters)) == null)
        assertFails { UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(randomCharacters, setOf(userName))) }
        assertFails { UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, UserGroupUserRequest(randomCharacters, userName)) }
        assertFails { UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(randomCharacters)) }
    }
}