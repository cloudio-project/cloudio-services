package ch.hevs.cloudio.cloud

import ch.hevs.cloudio.cloud.apiutils.*
import ch.hevs.cloudio.cloud.model.Authority
import ch.hevs.cloudio.cloud.model.Permission
import ch.hevs.cloudio.cloud.model.PermissionPriority
import ch.hevs.cloudio.cloud.model.PrioritizedPermission
import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.junit.Before
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootTest
class ApiUtilsTest() {

    @Autowired
    private lateinit var userRepository: UserRepository

    private var encoder: PasswordEncoder = BCryptPasswordEncoder()

    private val userName = "testUserName"

    private val userTest =  User(userName,
            encoder.encode("123456"),
            mapOf("bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/#" to PrioritizedPermission(Permission.OWN, PermissionPriority.HIGHEST),
                    "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/inside/temperature" to PrioritizedPermission(Permission.DENY, PermissionPriority.HIGH),
                    "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/*/temperatures/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                    "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/error/inside/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                    "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/temperatures/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW),
                    "bc0f1bf8-bdae-11e9-9cb5-2a2ae2dbcce4/Meteo/*/error/temperature" to PrioritizedPermission(Permission.GRANT, PermissionPriority.LOW)),
            setOf("testGroup1", "testGroup2"),
            setOf(Authority.BROKER_ADMINISTRATION, Authority.HTTP_ACCESS, Authority.HTTP_ADMIN))

    @Before
    fun setup() {
    }

    @Test
    fun userManagementUtilTest() {
        try {
            try {
                //be sure that the user is not in mongodb before we start this test
                UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            }
            catch (innerE: Exception){}

            //create an User
            UserManagementUtil.createUser(userRepository, userTest)
            try {
                UserManagementUtil.createUser(userRepository, userTest)
                assert(false)   //shouldn't be able to creat twice an user with same username
            }catch (cloudioApiException: CloudioApiException){
            }

            //test that user is in the user list
            var list = UserManagementUtil.getUserList(userRepository)
            assert(list.userList.contains(userName))

            //that that the user can be retreived
            var user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
            assert(user != null)
            assert(user?.userName == userName)

            //modify user password
            UserManagementUtil.modifyUserPassword(userRepository, UserPasswordRequest(userName, "password"))
            //test new password
            assert(UserManagementUtil.getUser(userRepository, UserRequest(userName))?.passwordHash == "password")

            //add authority
            UserManagementUtil.addUserAuthority(userRepository, AddAuthorityRequest(userName, setOf(Authority.BROKER_POLICYMAKER)))
            //test authority
            assert(UserManagementUtil.getUser(userRepository, UserRequest(userName))?.authorities?.contains(Authority.BROKER_POLICYMAKER) == true)

            //remove authority
            UserManagementUtil.removeUserAuthority(userRepository, RemoveAuthorityRequest(userName, Authority.BROKER_POLICYMAKER))
            //test authority is removed
            assert((UserManagementUtil.getUser(userRepository, UserRequest(userName))?.authorities?.contains(Authority.BROKER_POLICYMAKER)) == false)

            //remove user
            UserManagementUtil.deleteUser(userRepository, UserRequest(userName))
            //user shouldn't exist
            user = UserManagementUtil.getUser(userRepository, UserRequest(userName))
            assert(user == null)
            //user shouldn't be in the list
            list = UserManagementUtil.getUserList(userRepository)
            assert(!list.userList.contains(userName))
        }
        catch(e: Exception){
            e.printStackTrace()
            assert(false)
        }
    }
}