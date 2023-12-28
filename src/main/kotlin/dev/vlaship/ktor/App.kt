package dev.vlaship.ktor

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

internal val log = KtorSimpleLogger("App")

fun main(args: Array<String>) {
    log.info("Starting")
    EngineMain.main(args)
}

fun Application.serverModule() {
//    val isDevelopment = environment.config.propertyOrNull("ktor.development")?.getString()?.toBoolean() ?: false
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 18080
    val host = environment.config.propertyOrNull("ktor.deployment.host")?.getString() ?: "0.0.0.0"

    embeddedServer(Netty,
        port = port,
        host = host,
        watchPaths = listOf("classes"),
        configure = {
            connectionGroupSize = 2
            workerGroupSize = 5
            callGroupSize = 10
            shutdownGracePeriod = 2000
            shutdownTimeout = 3000
        }) {
        // modules
        install(Compression) {
            gzip {
                priority = 1.0
            }
            deflate {
                priority = 10.0
                minimumSize(1024) // condition
            }
        }
        install(CallLogging) {
            level = Level.INFO
            format { call ->
                runBlocking {
                    "Body: ${call.receiveText()}"
                }
            }
        }
        install(DefaultHeaders) {
            header("X-Engine", "Ktor")
        }
//        install(Locations)
        install(CallId) {
            header(HttpHeaders.XRequestId)
            verify { callId: String ->
                callId.isNotEmpty()
            }
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            }
        }
        configureRateLimiter()
        install(AutoHeadResponse)
        install(DoubleReceive)

        configureValidation()

        configureRouting()
        configureSerialization()
    }.start(wait = true)
}

fun Application.configureRateLimiter() {
    install(RateLimit) {
        global {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
        }
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
        customer()
    }
}

fun Route.customer() {
    get("/customer/{id}") {
        val id = call.parameters["id"]
        call.respond(Customer(id ?: "0", "John", "Smith", ""))
    }
    post("/customer") {
        val customer = call.receive<Customer>()
        call.respond(customer)
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureValidation() {
    install(RequestValidation) {
        validate {
            filter { body ->
                body is ByteArray
            }
            validation { body ->
                body as ByteArray
                if (body.isNotEmpty()) {
                    ValidationResult.Valid
                } else {
                    ValidationResult.Invalid("Body is empty")
                }
            }
        }
    }
}