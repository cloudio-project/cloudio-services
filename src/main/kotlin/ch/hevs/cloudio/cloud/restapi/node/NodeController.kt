package ch.hevs.cloudio.cloud.restapi.node

import ch.hevs.cloudio.cloud.security.Authority
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory
import java.net.InetAddress

@Profile("rest-api")
@Tag(name = "Node info", description = "Information about the cloud.iO service node.")
@RestController
@RequestMapping("api/v1/node")
@SecurityRequirement(name = "basicAuth")
class NodeController(
    private val buildProperties: BuildProperties
) {
    @GetMapping("/info", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns node info.")
    @ApiResponses(value = [
        ApiResponse(description = "Node information.", responseCode = "200", content = [Content(schema = Schema(type = "object", example = "{\n" +
                "  \"version\": \"feature/update-components-latest\",\n" +
                "  \"commit\": \"7fe39ec\",\n" +
                "  \"code\": \"https://github.com/cloudio-project/cloudio-services/commits/7fe39ece62ca1217911a46bb9b016e8e03d6fedb\",\n" +
                "  \"build date\": \"2022-09-05T13:51:13.983Z\",\n" +
                "  \"host\": \"Some machine\",\n" +
                "  \"address\": \"42.42.42.42\",\n" +
                "  \"jvm\": \"OpenJDK 64-Bit Server VM 18.0.1.1+2-6\",\n" +
                "  \"uptime\": 232946,\n" +
                "  \"pid\": 74235,\n" +
                "  \"memory\": {\n" +
                "    \"heap\": {\n" +
                "      \"init\": 268435456,\n" +
                "      \"used\": 113951984,\n" +
                "      \"comitted\": 251658240,\n" +
                "      \"max\": 4294967296\n" +
                "    },\n" +
                "    \"other\": {\n" +
                "      \"init\": 2555904,\n" +
                "      \"used\": 116342888,\n" +
                "      \"comitted\": 118095872\n" +
                "    }\n" +
                "  },\n" +
                "  \"threads\": 51,\n" +
                "  \"peak-threads\": 55\n" +
                "}"))])
    ])
    fun getNodeInfo(@Parameter(hidden = true) authentication: Authentication?) = mutableMapOf<String,Any>(
        "version" to buildProperties.version,
        "commit" to buildProperties["shortHash"],
        "code" to "https://github.com/cloudio-project/cloudio-services/commits/${buildProperties["hash"]}",
        "build date" to buildProperties.time
    ).apply {
        if (authentication?.authorities?.contains(SimpleGrantedAuthority(Authority.HTTP_ADMIN.toString())) == true) {
            set("host", InetAddress.getLocalHost().hostName)
            set("address", InetAddress.getLocalHost().hostAddress)
            ManagementFactory.getRuntimeMXBean().also {
                set("jvm", "${it.vmName} ${it.vmVersion}")
                set("uptime", it.uptime)
                set("pid", it.pid)
            }
            ManagementFactory.getMemoryMXBean().also {
                set("memory", mapOf(
                    "heap" to mapOf(
                        "init" to it.heapMemoryUsage.init,
                        "used" to it.heapMemoryUsage.used,
                        "comitted" to it.heapMemoryUsage.committed,
                        "max" to it.heapMemoryUsage.max
                    ),
                    "other" to mapOf(
                        "init" to it.nonHeapMemoryUsage.init,
                        "used" to it.nonHeapMemoryUsage.used,
                        "comitted" to it.nonHeapMemoryUsage.committed
                    )
                ))
            }
            ManagementFactory.getThreadMXBean().also {
                set("threads", it.threadCount)
                set("peak-threads", it.peakThreadCount)
            }
        }
    }
}
