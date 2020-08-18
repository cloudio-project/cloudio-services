import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    id("com.google.cloud.tools.jib") version "2.5.0"
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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
    implementation("org.postgresql:postgresql")
    implementation("com.vladmihalcea:hibernate-types-52:2.9.13")
    implementation("org.influxdb:influxdb-java")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.66")
    implementation("io.springfox:springfox-swagger2:3.0.0")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.72")
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
            val adminPassword: String? by project

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

            // PostgreSQL.
            environment("spring.datasource.url", "jdbc:postgresql://localhost:5432/cloudio")
            environment("spring.datasource.username", "cloudio")
            environment("spring.datasource.password", adminPassword ?: "admin")
            environment("jpa.hibernate.ddl-auto" ,"update")
        }
    }
}

tasks.test {
    dependsOn("cloudio-dev-environment:createDevServices")
    doFirst {
        tasks.test.configure {
            val adminPassword: String? by project

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

            // PostgreSQL.
            environment("spring.datasource.url", "jdbc:postgresql://localhost:5432/cloudiotest")
            environment("spring.datasource.username", "cloudio")
            environment("spring.datasource.password", adminPassword ?: "admin")
            environment("jpa.hibernate.ddl-auto" ,"create")

        }
    }
}

jib.to.image = "cloudio/${project.name}:${project.version}"
