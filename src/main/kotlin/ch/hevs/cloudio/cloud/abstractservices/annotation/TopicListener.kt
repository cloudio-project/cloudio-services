package ch.hevs.cloudio.cloud.abstractservices.annotation

import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class TopicListener(
    val topics: Array<String>
)
