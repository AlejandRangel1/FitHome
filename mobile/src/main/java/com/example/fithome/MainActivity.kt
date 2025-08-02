package com.example.fithome

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wearable.Wearable
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch

<<<<<<< Updated upstream


class MainActivity : AppCompatActivity(), SensorEventListener {
=======
// ¡IMPORTANTE! Añadimos MessageClient.OnMessageReceivedListener por si lo necesitas en el futuro
// Si no lo usas, no afecta en nada.
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent

class MainActivity : AppCompatActivity(), SensorEventListener, MessageClient.OnMessageReceivedListener {
>>>>>>> Stashed changes

    private val client = HttpClient(Android)
    private val tvIpAddress = "10.0.2.2"

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var isRoutineActive = false

    // --- Vistas de la UI ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var instructionText: TextView
    private lateinit var resetButton: Button
    private lateinit var instructionContainer: ConstraintLayout // ¡NUEVO! Referencia al contenedor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // --- Encontramos todas las vistas, incluyendo el nuevo contenedor ---
        recyclerView = findViewById(R.id.recyclerView)
        instructionText = findViewById(R.id.instructionText)
        resetButton = findViewById(R.id.resetButton)
        instructionContainer = findViewById(R.id.instructionContainer) // ¡NUEVO!

        val exerciseList = listOf(
            Exercise(name = "Sentadillas", durationInSeconds = 60, videoId = "squats_video", repGoal = 10),
            Exercise(name = "Flexiones", durationInSeconds = 90, videoId = "pushups_video", repGoal = 15),
            Exercise(name = "Plancha", durationInSeconds = 60, videoId = "plank_video", repGoal = 0),
            Exercise(name = "Saltos de Tijera", durationInSeconds = 50, videoId = "jumping_jacks_video", repGoal = 20),
            Exercise(name = "Zancadas", durationInSeconds = 60, videoId = "lunges_video", repGoal = 12),
            Exercise(name = "Burpees", durationInSeconds = 40, videoId = "burpees_video", repGoal = 10),
            Exercise(name = "Mountain Climbers", durationInSeconds = 45, videoId = "mountain_climbers_video", repGoal = 20),
            Exercise(name = "Skipping", durationInSeconds = 50, videoId = "skipping_video", repGoal = 25),
            Exercise(name = "Puente de Glúteos", durationInSeconds = 45, videoId = "glute_bridge_video", repGoal = 15),
            Exercise(name = "Elevaciones de Pierna", durationInSeconds = 40, videoId = "leg_raises_video", repGoal = 12),
            Exercise(name = "Abdominales", durationInSeconds = 60, videoId = "abdominales_video", repGoal = 1)
        )
// Cambio realizado por Eduardo

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ExerciseAdapter(exerciseList) { selectedExercise ->
            sendRoutineConfigToWear(selectedExercise)
            sendStartCommandToTV(selectedExercise)
            prepareForSmartStart()
        }

        resetButton.setOnClickListener {
            Log.d("FitHome-Mobile", "Botón de Finalizar pulsado. Reiniciando todo.")
            sendStopCommandToWear()
            sendStopCommandToTV()
            resetToSelectionScreen()
        }
    }

    // --- Lógica de UI actualizada para usar el contenedor ---
    private fun prepareForSmartStart() {
        isRoutineActive = true
        recyclerView.visibility = View.GONE
        instructionContainer.visibility = View.VISIBLE // Mostramos el contenedor
        instructionText.text = "¡Rutina enviada!\n\nColoca el teléfono boca abajo para empezar."
    }

    private fun resetToSelectionScreen() {
        instructionContainer.visibility = View.GONE // Ocultamos el contenedor
        recyclerView.visibility = View.VISIBLE
        findViewById<TextView>(R.id.textView).text = "Elige otra rutina"
    }

    // --- El resto de la lógica se mantiene intacta ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            if (isRoutineActive && lightValue < 5.0f) {
                isRoutineActive = false
                sendStartCommandToWear()
                instructionText.text = "¡Vamos! ¡Tú puedes!"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        lightSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        // Añadimos el listener de mensajes por si lo necesitamos en el futuro
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        // Quitamos el listener de mensajes para ahorrar batería
        Wearable.getMessageClient(this).removeListener(this)
    }

    // Función para manejar mensajes del reloj (actualmente no se usa, pero es bueno tenerla)
    override fun onMessageReceived(messageEvent: MessageEvent) {
        // Podrías añadir lógica aquí en el futuro si el reloj necesita enviar un mensaje
    }

    private fun sendStopCommandToWear() {
        sendMessageToWear("/stop_routine", null)
    }

    private fun sendStopCommandToTV() {
        lifecycleScope.launch {
            try {
                val url = "http://$tvIpAddress:8080/stop"
                client.get(url)
            } catch (e: Exception) {
                Log.e("FitHome-Mobile", "Error al enviar comando de parada a la TV", e)
            }
        }
    }

    private fun sendRoutineConfigToWear(exercise: Exercise) {
        val payload = "${exercise.name};${exercise.durationInSeconds};${exercise.repGoal}".toByteArray()
        sendMessageToWear("/start_routine", payload)
    }

    private fun sendStartCommandToWear() {
        sendMessageToWear("/start_timer", null)
    }

    private fun sendMessageToWear(path: String, data: ByteArray?) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(this).sendMessage(node.id, path, data)
                    .addOnSuccessListener { Log.d("FitHome-Mobile", "Mensaje '$path' enviado a ${node.displayName}") }
            }
        }
    }

    private fun sendStartCommandToTV(exercise: Exercise) {
        lifecycleScope.launch {
            try {
                val url = "http://$tvIpAddress:8080/play?video=${exercise.videoId}"
                client.get(url)
            } catch (e: Exception) {
                Log.e("FitHome-Mobile", "Error al conectar con la TV", e)
            }
        }
    }
}