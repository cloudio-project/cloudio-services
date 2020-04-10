package ch.hevs.cloudio.cloud.dao

import java.util.regex.Pattern.compile
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class EmailAddress(
        @Column(name = "email_address")
        private val address: String = "") {

    fun isValid() = emailRegex.matcher(address).matches()

    companion object {
        private val emailRegex = compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        )
    }
}
