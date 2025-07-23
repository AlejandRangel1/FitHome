package com.example.tv

import android.util.Log
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.isActive

class WebServer(private val onPlayVideo: (String) -> Unit) {

    // Solo declaramos la variable, no la inicializamos aquí.
    private var server: CIOApplicationEngine? = null

    fun start() {
        // Si el servidor ya está corriendo, no hacemos nada.
        if (server?.application?.isActive == true) {
            Log.d("FitHome-TV-Server", "El servidor ya está activo.")
            return
        }

        Log.d("FitHome-TV-Server", "Creando y arrancando el servidor en hilo de fondo...")
        // Creamos y arrancamos el servidor aquí, dentro del hilo de fondo.
        server = embeddedServer(CIO, port = 8080) {
            routing {
                get("/play") {
                    val videoId = call.request.queryParameters["video"]
                    if (videoId != null) {
                        Log.d("FitHome-TV-Server", "¡COMANDO RECIBIDO!: $videoId")
                        onPlayVideo(videoId)
                        call.respondText("OK, reproduciendo $videoId")
                    } else {
                        call.respondText("Error: Parámetro 'video' no encontrado")
                    }
                }
            }
        }
        server?.start(wait = false)
        Log.d("FitHome-TV-Server", "El servidor ha sido iniciado.")
    }

    fun stop() {
        Log.d("FitHome-TV-Server", "Deteniendo el servidor...")
        server?.stop(1000, 2000)
        server = null // Lo ponemos a null para que se pueda recrear.
    }
}