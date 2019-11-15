package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserManagementUtil {

    fun createUser(userRepository: UserRepository, newUser: User): ApiActionAnswer{
        //prevent creation of two user with same username
        if(userRepository.findByIdOrNull(newUser.userName)!=null)
            return ApiActionAnswer(false, newUser.userName+" already exists")
        else {
            userRepository.save(newUser)
            return ApiActionAnswer(true, "")
        }
    }

    fun getUser(userRepository: UserRepository, userRequest: UserRequest):User?{
        return userRepository.findByIdOrNull(userRequest.userName)
    }

    fun deleteUser(userRepository: UserRepository, userRequest: UserRequest):ApiActionAnswer{
        val userToDelete = userRepository.findByIdOrNull(userRequest.userName)
        return if(userToDelete != null) {
            userRepository.delete(userToDelete)
            ApiActionAnswer(true, "")
        }
        else
            ApiActionAnswer(false, userRequest.userName+" doesn't exist")
    }
    fun modifyUserPassword(userRepository: UserRepository, userPasswordRequest: UserPasswordRequest): ApiActionAnswer {
        val userToModify = userRepository.findByIdOrNull(userPasswordRequest.userName)
        return if(userToModify != null) {
            userToModify.passwordHash = userPasswordRequest.passwordHash
            userRepository.save(userToModify)

            ApiActionAnswer(true, "")
        }
        else
            ApiActionAnswer(false, userPasswordRequest.userName+" doesn't exist")
    }

    fun addUserAuthority(userRepository: UserRepository, addAuthorityRequest: AddAuthorityRequest): ApiActionAnswer {
        val userToModify = userRepository.findByIdOrNull(addAuthorityRequest.userName)
        return if(userToModify != null) {
            val authorities = userToModify.authorities.toMutableSet()
            authorities.addAll(addAuthorityRequest.authorities)
            userToModify.authorities = authorities
            userRepository.save(userToModify)

            ApiActionAnswer(true, "")
        }
        else
            ApiActionAnswer(false, addAuthorityRequest.userName+" doesn't exist")
    }

    fun removeUserAuthority(userRepository: UserRepository, removeAuthorityRequest: RemoveAuthorityRequest): ApiActionAnswer {
        val userToModify = userRepository.findByIdOrNull(removeAuthorityRequest.userName)
        return if(userToModify != null) {
            val authorities = userToModify.authorities.toMutableSet()
            val removeResult = authorities.remove(removeAuthorityRequest.authority)
            if(removeResult){
                userToModify.authorities = authorities
                userRepository.save(userToModify)
                ApiActionAnswer(true, "")
            }
            else
                ApiActionAnswer(false, removeAuthorityRequest.userName+" doesn't have Authority ${removeAuthorityRequest.authority}")
        }
        else
            ApiActionAnswer(false, removeAuthorityRequest.userName+" doesn't exist")
    }

    fun getUserList(userRepository: UserRepository): UserListAnswer {
        val userList : MutableSet<String> = mutableSetOf()
        userRepository.findAll().forEach { user ->  userList.add(user.userName) }
        return UserListAnswer(userList)
    }

}