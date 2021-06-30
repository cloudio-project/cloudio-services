package ch.hevs.cloudio.cloud

object TestUtil {

    private val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generateRandomString(length: Int): String {
        return List(length) { alphabet.random() }.joinToString("")
    }
}