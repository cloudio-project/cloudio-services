package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserGroupUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    @Test
    fun userGroupUtilTest() {
        val userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)

        val userGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName, emptySet())

        try {
            //be sure that the user group is not in mongodb before we start this test
            UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))
        } catch (innerE: Exception) {
        }

        try {
            //be sure that the user is not in mongodb before we start this test
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
        } catch (innerE: Exception) {
        }
        //create an User
        UserManagementUtil.createUser(userRepository, userTest)

        //create an User Group
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)
        try {
            UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)
            assert(false)   //shouldn't be able to create twice an user group with same user group name
        } catch (cloudioApiException: CloudioApiException) {
        }

        //test that we can retrieve user group
        var userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        assert(userGroup != null)
        assert(userGroup?.userGroupName == userGroupName)
        //test that we can retrieve random name user group
        userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(randomCharacters))
        assert(userGroup == null)


        //test that user is in the user list
        var list = UserGroupUtil.getUserGroupList(userGroupRepository)
        assert(list.userGroupList.contains(userGroupName))

        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName, setOf(userName)))

        userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        var user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
        //test that user is part of usergroup and vice-versa
        assert(userGroup!!.usersList.contains(userName))
        assert(user!!.userGroups.contains(userGroupName))

        //remove user from user group
        UserGroupUtil.deleteUserToGroup(userGroupRepository, userRepository, UserGroupUserRequest(userGroupName, userName))

        userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
        //test that user is not part of usergroup and vice-versa
        assert(!userGroup!!.usersList.contains(userName))
        assert(!user!!.userGroups.contains(userGroupName))

        //add user to user group
        UserGroupUtil.addUserToGroup(userGroupRepository, userRepository, UserGroupUserRequestList(userGroupName, setOf(userName)))
        //delet user group
        UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))

        userGroup = UserGroupUtil.getUserGroup(userGroupRepository, UserGroupRequest(userGroupName))
        user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
        //test that user is not part of usergroup and that user group doesn't exist
        assert(userGroup == null)
        assert(!user!!.userGroups.contains(userGroupName))

        try {
            //remove users and user group
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))
        } catch (e: Exception) {
        }

        //user group shouldn't be in the list
        list = UserGroupUtil.getUserGroupList(userGroupRepository)
        assert(!list.userGroupList.contains(userGroupName))

    }
}