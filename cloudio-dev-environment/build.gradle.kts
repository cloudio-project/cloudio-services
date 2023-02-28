tasks.register<Exec>("createDevCertificates") {
    group = "dev-certificates"
    workingDir = file("certificates")
    commandLine = listOf("make", "all")
}

tasks.register<Exec>("cleanDevCertificates") {
    group = "dev-certificates"
    workingDir = file("certificates")
    commandLine = listOf("make", "clean")
}

tasks.register<Exec>("createDevServices") {
    group = "dev-services"
    dependsOn("createDevCertificates")
    workingDir = file(".")
    doFirst {
        val adminPassword: String? by project
        environment["ADMIN_PASSWORD"] = adminPassword ?: "admin_password"
        environment["CA_CERT"] = file("certificates/ca.cer").readText()
        environment["SERVER_CERT"] = file("certificates/server.cer").readText()
        environment["SERVER_KEY"] = file("certificates/server.key").readText()
    }
    commandLine = listOf("docker-compose", "up", "-d")
}

tasks.register<Exec>("pauseDevServices") {
    group = "dev-services"
    workingDir = file(".")
    commandLine = listOf("docker-compose", "stop")
}

tasks.register<Exec>("resumeDevServices") {
    group = "dev-services"
    workingDir = file(".")
    commandLine = listOf("docker-compose", "start")
}

tasks.register<Exec>("destroyDevServices") {
    group = "dev-services"
    workingDir = file(".")
    doFirst {
        environment["CA_CERT"] = ""
        environment["SERVER_CERT"] = ""
        environment["SERVER_KEY"] = ""
    }
    commandLine = listOf("docker-compose", "down")
}
