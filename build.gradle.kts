import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    id("com.google.cloud.tools.jib") version "3.1.4"
}

open class GitTools(p: String) {
    val tag: String? by lazy {
        Runtime.getRuntime().exec("git describe --exact-match --tags HEAD").let {
            it.waitFor()
            if (it.exitValue() == 0) String(it.inputStream.readAllBytes()).trim() else null
        }
    }
    val hash: String by lazy { String(Runtime.getRuntime().exec("git rev-parse HEAD").inputStream.readAllBytes()).trim() }
    val shortHash: String by lazy { String(Runtime.getRuntime().exec("git rev-parse --short HEAD").inputStream.readAllBytes()).trim() }
    val branch: String by lazy { String(Runtime.getRuntime().exec("git name-rev --name-only HEAD").inputStream.readAllBytes()).trim().replace("remotes/origin/", "") }
}
val git: GitTools by project
project.extensions.create("git", GitTools::class.java, "")

group = "ch.hevs.cloudio"
version = git.tag ?: "${git.branch}-latest"
java.sourceCompatibility = JavaVersion.VERSION_11

springBoot {
    buildInfo {
        properties {
            additional = mapOf(
                "hash" to git.hash,
                "shortHash" to git.shortHash
            )
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.springfox:springfox-boot-starter:3.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
    implementation("org.postgresql:postgresql")
    implementation("com.vladmihalcea:hibernate-types-52:2.9.13")
    implementation("org.influxdb:influxdb-java")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.66")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.72")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("co.nstant.in:cbor:0.9")}

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
            environment("spring.rabbitmq.ssl.verify-hostname", "false")

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
            environment("spring.rabbitmq.ssl.verify-hostname", "false")

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

jib {
    from {
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
            platform {
                architecture = "arm"
                os = "linux"
            }
        }
    }
    to {
        image = "cloudio/${project.name}:${project.version}"
        tags = if (git.tag != null) setOf("latest") else setOf()
    }
}
