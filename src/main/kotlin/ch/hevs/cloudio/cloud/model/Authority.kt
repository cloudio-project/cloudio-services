package ch.hevs.cloudio.cloud.model

import org.springframework.security.access.prepost.PreAuthorize

enum class Authority {
    BROKER_ACCESS,
    BROKER_MANAGEMENT_POLICYMAKER,
    BROKER_MANAGEMENT_MONITORING,
    BROKER_MANAGEMENT_ADMINISTRATOR,

    HTTP_ACCESS,
    HTTP_ADMIN;

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @PreAuthorize("hasAuthority('HTTP_ADMIN')")
    annotation class HttpAdmin
}
