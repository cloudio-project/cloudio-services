import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.2.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    id("com.google.cloud.tools.jib") version "2.0.0"
}

group = "ch.hevs.cloudio"
version = "0.2.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

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
    implementation("org.influxdb:influxdb-java")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.64")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.61")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.register("bootRunDev") {
    group = "application"
    description = "Runs this project as a Spring Boot application with the cloudio dev environment"
    dependsOn("cloudio-dev-environment:createDevServices")
    doFirst {
        tasks.bootRun.configure {
            // Certificate manager.
            environment("cloudio.caCertificate", file("cloudio-dev-environment/certificates/ca.cer").readText())
            environment("cloudio.caPrivateKey", file("cloudio-dev-environment/certificates/ca.key").readText())
            environment("cloudio.caCertificateJksPath", "cloudio-dev-environment/certificates/ca.jks")
            environment("cloudio.caCertificateJksPassword", "123456")

            // RabbitMQ (AMQPs).
            environment("spring.rabbitmq.host", "localhost")
            environment("spring.rabbitmq.port", "5671")
            environment("spring.rabbitmq.ssl.enabled", "true")
            environment("spring.rabbitmq.ssl.algorithm", "TLSv1.2")
            environment("spring.rabbitmq.ssl.verify-hostname", "false")
            environment("spring.rabbitmq.ssl.validate-server-certificate", "true")

            // TODO: Certificate based login does not work yet, for a reason the client does not send his certificate.
            //environment("spring.rabbitmq.ssl.key-store-type", "PKCS12")
            //environment("spring.rabbitmq.ssl.key-store", "file:cloudio-dev-environment/certificates/cloudio_services.p12")
            environment("spring.rabbitmq.username", "admin")
            val adminPassword: String? by project
            environment("spring.rabbitmq.password", adminPassword ?: "admin")

            environment("spring.rabbitmq.ssl.trust-store-type", "JKS")
            environment("spring.rabbitmq.ssl.trust-store", "file:cloudio-dev-environment/certificates/ca.jks")
            environment("spring.rabbitmq.ssl.trust-store-password", "cloudioDEV")

            // InfluxDB client (HTTP).
            environment("spring.influx.url", "http://localhost:8086")
            environment("cloudio.influxBatchIntervallMs", "3000")
            environment("cloudio.influxBatchSize", "2000")

            // MongoDB client (HTTP).
            environment("spring.data.mongodb.host", "localhost")

            // Web server.
            environment("server.port", "8081")
        }
    }
    finalizedBy("bootRun")
}
