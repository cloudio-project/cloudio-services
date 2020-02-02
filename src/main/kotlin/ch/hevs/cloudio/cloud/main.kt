package ch.hevs.cloudio.cloud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CloudioApplication

fun main(args: Array<String>) {
    runApplication<CloudioApplication>(*args)
}
