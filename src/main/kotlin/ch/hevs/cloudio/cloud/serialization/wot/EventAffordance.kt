package ch.hevs.cloudio.cloud.serialization.wot

data class EventAffordance(
        val data: DataSchemaInterface,
        override val forms: Set<Form>
): InteractionAffordanceInterface