import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
    id("com.google.cloud.tools.jib") version "2.1.0"
}

group = "ch.hevs.cloudio"
version = "0.2.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

springBoot {
    buildInfo()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
    implementation("org.influxdb:influxdb-java")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.64")
    implementation("io.springfox:springfox-swagger2:2.9.2")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.71")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

tasks.bootRun {
    dependsOn("cloudio-dev-environment:createDevServices")
    doFirst {
        tasks.bootRun.configure {
            // Certificate manager.
            environment("cloudio.cert-manager.caCertificate", file("cloudio-dev-environment/certificates/ca.cer").readText())
            environment("cloudio.cert-manager.caPrivateKey", file("cloudio-dev-environment/certificates/ca.key").readText())

            // RabbitMQ (AMQPs).
            environment("spring.rabbitmq.host", "localhost")
            environment("spring.rabbitmq.ssl.key-store", "file:./cloudio-dev-environment/certificates/cloudio_services.p12")
            environment("spring.rabbitmq.ssl.verify-hostname", "false")
            environment("spring.rabbitmq.ssl.trust-store", "file:./cloudio-dev-environment/certificates/ca.jks")
            environment("spring.rabbitmq.ssl.trust-store-password", "cloudioDEV")

            // InfluxDB.
            environment("spring.influx.url", "http://localhost:8086")

            // MongoDB.
            environment("spring.data.mongodb.host", "localhost")
        }
    }
}

tasks.test {
    dependsOn("cloudio-dev-environment:createDevServices")
    doFirst {
        tasks.test.configure {
            // Certificate manager.
            environment("cloudio.cert-manager.caCertificate", file("cloudio-dev-environment/certificates/ca.cer").readText())
            environment("cloudio.cert-manager.caPrivateKey", file("cloudio-dev-environment/certificates/ca.key").readText())

            // RabbitMQ (AMQPs).
            environment("spring.rabbitmq.host", "localhost")
            environment("spring.rabbitmq.ssl.key-store", "file:./cloudio-dev-environment/certificates/cloudio_services.p12")
            environment("spring.rabbitmq.ssl.verify-hostname", "false")
            environment("spring.rabbitmq.ssl.trust-store", "file:./cloudio-dev-environment/certificates/ca.jks")
            environment("spring.rabbitmq.ssl.trust-store-password", "cloudioDEV")

            // InfluxDB.
            environment("spring.influx.url", "http://localhost:8086")
            environment("cloudio.influx.database", "cloudiotest")

            // MongoDB.
            environment("spring.data.mongodb.host", "localhost")
            environment("spring.data.mongodb.database", "cloudiotest")
        }
    }
}

jib.to.image = "cloudio/${project.name}:${project.version}"
