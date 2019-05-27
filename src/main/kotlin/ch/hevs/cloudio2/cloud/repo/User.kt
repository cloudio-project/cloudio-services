package ch.hevs.cloudio2.cloud.repo

import ch.hevs.cloudio2.cloud.model.Permission
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.HashMap


@Document
class User {
    @Id
    var userName: String? = null

    var passwordHash: String? = null

    var permissions: Map<String, Permission>

    constructor() {
        permissions = HashMap()
    }

    constructor(userName: String, passwordHash: String) {
        this.userName = userName
        this.passwordHash = passwordHash
        permissions = HashMap()
    }
}
