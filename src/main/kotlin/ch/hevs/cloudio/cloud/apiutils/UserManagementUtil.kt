package ch.hevs.cloudio.cloud.apiutils

import ch.hevs.cloudio.cloud.repo.authentication.User
import ch.hevs.cloudio.cloud.repo.authentication.UserRepository
import org.springframework.data.repository.findByIdOrNull

object UserManagementUtil {

    fun createUser(userRepository: UserRepository, newUser: User): ApiActionAnswer{
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

}