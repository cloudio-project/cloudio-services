package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserManagementUtil {

    @Throws(CloudioApiException::class)
    fun createUser(userRepository: UserRepository, newUser: User){
        //prevent creation of two user with same username
        if(userRepository.findByIdOrNull(newUser.userName)!=null)
            throw CloudioApiException(newUser.userName+" already exists")
        else {
            userRepository.save(newUser)
        }
    }

    fun getUser(userRepository: UserRepository, userRequest: UserRequest):User?{
        return userRepository.findByIdOrNull(userRequest.userName)
    }

    @Throws(CloudioApiException::class)
    fun deleteUser(userRepository: UserRepository, userRequest: UserRequest){
        val userToDelete = userRepository.findByIdOrNull(userRequest.userName)
        return if(userToDelete != null) {
            userRepository.delete(userToDelete)
        }
        else
            throw CloudioApiException(userRequest.userName+" doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun modifyUserPassword(userRepository: UserRepository, userPasswordRequest: UserPasswordRequest){
        val userToModify = userRepository.findByIdOrNull(userPasswordRequest.userName)
        if(userToModify != null) {
            userToModify.passwordHash = userPasswordRequest.passwordHash
            userRepository.save(userToModify)
        }
        else
            throw CloudioApiException(userPasswordRequest.userName+" doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun addUserAuthority(userRepository: UserRepository, addAuthorityRequest: AddAuthorityRequest){
        val userToModify = userRepository.findByIdOrNull(addAuthorityRequest.userName)
        if(userToModify != null) {
            val authorities = userToModify.authorities.toMutableSet()
            authorities.addAll(addAuthorityRequest.authorities)
            userToModify.authorities = authorities
            userRepository.save(userToModify)
        }
        else
            throw CloudioApiException(addAuthorityRequest.userName+" doesn't exist")
    }

    @Throws(CloudioApiException::class)
    fun removeUserAuthority(userRepository: UserRepository, removeAuthorityRequest: RemoveAuthorityRequest){
        val userToModify = userRepository.findByIdOrNull(removeAuthorityRequest.userName)
        if(userToModify != null) {
            val authorities = userToModify.authorities.toMutableSet()
            val removeResult = authorities.remove(removeAuthorityRequest.authority)
            if(removeResult){
                userToModify.authorities = authorities
                userRepository.save(userToModify)
            }
            else
                throw CloudioApiException(removeAuthorityRequest.userName+" doesn't have Authority ${removeAuthorityRequest.authority}")
        }
        else
            throw CloudioApiException(removeAuthorityRequest.userName+" doesn't exist")
    }

    fun getUserList(userRepository: UserRepository): UserListAnswer {
        val userList : MutableSet<String> = mutableSetOf()
        userRepository.findAll().forEach { user ->  userList.add(user.userName) }
        return UserListAnswer(userList)
    }

}