package ch.hevs.cloudio.cloud.apiutilstest

import ch.hevs.cloudio.cloud.TestUtil
import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
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
class UserGroupAccessControlUtilTest {

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
        //create User and user group
        userName = TestUtil.generateRandomString(15)
        val userTest = TestUtil.createUser(userName)

        userGroupName = TestUtil.generateRandomString(15)
        val userGroupTest = TestUtil.createUserGroup(userGroupName, emptySet())

        UserManagementUtil.createUser(userRepository, userTest)
        UserGroupUtil.createUserGroup(userGroupRepository, userRepository, userGroupTest)
    }

    @AfterAll
    fun cleanUp() {
        try {
            //remove user and user group
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            UserGroupUtil.deleteUserGroup(userGroupRepository, userRepository, UserGroupRequest(userGroupName))
        } catch (e: Exception) {
        }
    }

    @Test
    fun addUserGroupAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))
    }

    @Test
    fun getUserGroupAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        val accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if added access control exist
        assert(accessRight?.get(topic) != null)
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
        assert(accessRight.get(topic)!!.permission == Permission.CONFIGURE)
        //test if random access control doesn't exist
        assert(accessRight.get(randomCharacters) == null)
    }

    @Test
    fun modifyUserGroupAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"

        UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequest(userGroupName, UserGroupRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        val accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
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
    }

    @Test
    fun removeUserGroupAccessRight() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))

        UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, UserGroupTopicRequest(userGroupName, topic))
        val accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if added access control exist
        assert(accessRight?.get(topic) == null)
    }

    @Test
    fun giveUserGroupAccessRightOwn() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        //give own right on topic to username
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.OWN, PermissionPriority.HIGH))))
        // give user right from userName to user group
        UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository,
                UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        val accessRight = UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(userGroupName))
        //test if parameters are correct
        assert(accessRight!!.get(topic)!!.priority == PermissionPriority.HIGH)
        assert(accessRight.get(topic)!!.permission == Permission.READ)
    }

    @Test
    fun giveUserGroupAccessRightNotOwn() {
        val topic = "${TestUtil.generateRandomString(15)}/#"
        //give own right on topic to username
        UserAccessControlUtil.addUserAccessRight(userRepository,
                UserRightRequestList(userName, setOf(UserRightTopic(topic, Permission.READ, PermissionPriority.HIGH))))

        // try to give user right from userName to usergroup, doesn't have own access modified above
        assertFails {
            UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository,
                    UserGroupRightRequestList(userGroupName, setOf(UserGroupRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        }
    }

    @Test
    fun randomCharacterUserGroupAccessTest() {
        val topic = "${TestUtil.generateRandomString(15)}/#"

        //shouldn't be able to act on random user group
        assert(UserGroupAccessControlUtil.getUserGroupAccessRight(userGroupRepository, UserGroupRequest(randomCharacters)) == null)
        assertFails {
            UserGroupAccessControlUtil.addUserGroupAccessRight(userGroupRepository,
                    UserGroupRightRequestList(randomCharacters, setOf(UserGroupRightTopic(topic, Permission.CONFIGURE, PermissionPriority.HIGH))))
        }
        assertFails {
            UserGroupAccessControlUtil.modifyUserGroupAccessRight(userGroupRepository,
                    UserGroupRightRequest(randomCharacters, UserGroupRightTopic(topic, Permission.DENY, PermissionPriority.LOW)))
        }
        assertFails { UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, UserGroupTopicRequest(randomCharacters, topic)) }
        assertFails { UserGroupAccessControlUtil.removeUserGroupAccessRight(userGroupRepository, UserGroupTopicRequest(userGroupName, randomCharacters)) }
        assertFails {
            UserGroupAccessControlUtil.giveUserGroupAccessRight(userGroupRepository, userRepository,
                    UserGroupRightRequestList(randomCharacters, setOf(UserGroupRightTopic(topic, Permission.READ, PermissionPriority.HIGH))), userName)
        }
    }
}