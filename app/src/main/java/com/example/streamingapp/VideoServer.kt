package com.example.streamingapp

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class VideoServer {
    private var server: ApplicationEngine? = null

    fun start() {
        if (server != null) return
        
        server = embeddedServer(CIO, port = 8080) {
            routing {
                get("/video") {
                    // Redirecting to a stable stream to simulate local hosting.
                    call.respondRedirect("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8")
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
}
