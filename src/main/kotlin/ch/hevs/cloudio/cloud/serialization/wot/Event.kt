package ch.hevs.cloudio.cloud.serialization.wot

data class Event(
        val data: Data,
        val forms: Set<Form>
)