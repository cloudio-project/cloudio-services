package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
import ch.hevs.cloudio.cloud.repo.authentication.UserGroupRepository
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertFails

@SpringBootTest
class UserGroupAccessControlUtilTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGroupRepository: UserGroupRepository

    //randomChar to retrieve non possible data
    private val randomCharacters: String = TestUtil.generateRandomString(15)

    @Test
    fun userGroupAccessControlUtilTest() {

        val userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)

        val userGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName, emptySet())

        val topic = "topic/#"

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

        UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        var accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if added access control exist
        assert(accessRight?.get(topic) != null)
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
        assert(accessRight.get(topic)!!.permission == Permission.CONFIGURE)
        //test if random access control doesn't exist
        assert(accessRight.get(randomCharacters) == null)

        UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequest(userGroupName, UserGroupRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if added access control exist
        assert(accessRight?.get(topic) != null)
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.LOW)
        assert(accessRight.get(topic)!!.permission == Permission.DENY)
        //shoudln't be able to modify random user group access control
        assertFails {
            UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository,
                    UserGroupRightRequest(randomCharacters, UserGroupRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        }

        UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, UserGroupTopicRequest(userGroupName, topic))
        accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if added access control exist
        assert(accessRight?.get(topic) == null)

        //give own right on topic to username
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.OWN, PermissionPriority.HIGH))))
        // give user right from userName to user group
        UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
        assert(accessRight.get(topic)!!.permission == Permission.READ)

        UserAccessControlUtil.modifyUserAccessRight(userRepository,
                UserRightRequest(userName, UserRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        // try to give user right from userName to usergroup, doesn't have own access modified above
        assertFails {
            UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository,
                    UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        }


        //shouldn't be able to act on random user group
        assert(UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(randomCharacters))==null)
        assertFails {
            UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                    UserGroupRightRequestList(randomCharacters, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))
        }
        assertFails {
            UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequest(randomCharacters, UserGroupRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        }
        assertFails {UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, UserGroupTopicRequest(randomCharacters, topic))}
        assertFails {UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, UserGroupTopicRequest(userGroupName, randomCharacters))}
        assertFails {
            UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository,
                UserGroupRightRequestList(randomCharacters, setOf(UserGroupRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        }





        try {
            //remove users and user group
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))
        } catch (e: Exception) {
        }
    }
}