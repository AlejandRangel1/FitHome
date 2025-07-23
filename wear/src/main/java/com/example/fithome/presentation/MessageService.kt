package com.example.fithome.presentation

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class MessageService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            // Este es el comando que ya teníamos para configurar la rutina
            "/start_routine" -> {
                val payload = String(messageEvent.data)
                Log.d("FitHome-Wear", "Comando de configuración recibido: $payload")

                val intent = Intent(this, ChronometerActivity::class.java).apply {
                    putExtra("ROUTINE_DATA", payload)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
            // ¡NUEVO COMANDO! Para iniciar el temporizador
            "/start_timer" -> {
                Log.d("FitHome-Wear", "Comando de INICIO recibido!")
                // Enviamos un "broadcast" que nuestra actividad podrá escuchar
                val intent = Intent("com.example.fithome.START_TIMER")
                sendBroadcast(intent)
            }
            "/stop_routine" -> {
                Log.d("FitHome-Wear", "Comando de PARADA recibido!")
                val intent = Intent("com.example.fithome.STOP_ROUTINE")
                sendBroadcast(intent)
            }
            else -> super.onMessageReceived(messageEvent)
        }
    }
}