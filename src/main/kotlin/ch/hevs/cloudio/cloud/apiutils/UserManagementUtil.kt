package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserManagementUtil {

    fun createUser(userRepository: UserRepository, newUser: User){
        userRepository.save(newUser)
    }

    fun getUser(userRepository: UserRepository, userRequest: UserRequest):User?{
        return userRepository.findByIdOrNull(userRequest.userName)
    }

    fun deleteUser(userRepository: UserRepository, userRequest: UserRequest):Boolean{
        val userToDelete = userRepository.findByIdOrNull(userRequest.userName)
        return if(userToDelete != null) {
            userRepository.delete(userToDelete)
            true
        }
        else
            false
    }

}