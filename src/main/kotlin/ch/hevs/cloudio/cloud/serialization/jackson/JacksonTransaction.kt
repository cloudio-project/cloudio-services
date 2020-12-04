package ch.hevs.cloudio.cloud.serialization.jackson

import ch.hevs.cloudio.cloud.model.Transaction
import java.util.*

data class JacksonTransaction(
        val attributes: Optional<Map<String, JacksonAttribute>> = Optional.empty()
) {
    fun toTransaction() = Transaction(
        attributes = attributes.get().mapValues { it.value.toAttribute() }.toMutableMap()
    )
}
