package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import ch.hevs.cloudio.cloud.restapi.admin.group.GroupManagementController
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

@RunWith(SpringRunner::class)
@SpringBootTest
class UserGroupUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    @Autowired
    private lateinit var userManagement: UserManagementController

    @Autowired
    private lateinit var groupManagement: GroupManagementController

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    private lateinit var userName: String
    private lateinit var userGroupName: String

    @Before
    fun setup() {
        userName = TestUtil.generateRandomString(15)

        userGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName)

        //create an User
        userManagement.postUserByUserName(userName, PostUserBody(password = "test"))

        //create an User Group
        groupManagement.postGroupByGroupName(userGroupName)
        groupManagement.putGroupByGroupName(userGroupName, userGroupTest)
    }

    @After
    fun cleanUp() {
        //remove users and user group
        userManagement.deleteUser(userName)
        groupManagement.deleteGroupByGroupName(userGroupName)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun createUserGroup() {
        val createUserGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(createUserGroupName)
        groupManagement.postGroupByGroupName(userGroupName)
        groupManagement.putGroupByGroupName(userGroupName, userGroupTest)

        groupManagement.deleteGroupByGroupName(userGroupName)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUserGroup() {
        //test that we can retrieve user group
        val userGroup = groupManagement.getGroupByGroupName(userGroupName)
        assert(userGroup.name == userGroupName)
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun getUserGroupList() {
        //test that user is in the user list
        val list = groupManagement.getAllGroups()
        assert(list.contains(userGroupName))
    }

    @Test
    @WithMockUser(username ="root", authorities = ["HTTP_ACCESS", "HTTP_ADMIN"])
    fun randomCharacterUserGroupTest() {
        //test that we can retrieve random name user group
        // TODO
        //assert(UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(randomCharacters)) == null)
        //assertFails { UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(randomCharacters, setOf(userName))) }
        //assertFails { UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, UserGroupUserRequest(randomCharacters, userName)) }
        //assertFails { UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(randomCharacters)) }
    }
}