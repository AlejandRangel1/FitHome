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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wearable.Wearable
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch



class MainActivity : AppCompatActivity(), SensorEventListener {

    private val client = HttpClient(Android)
    private val tvIpAddress = "10.0.2.2"

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var isRoutineActive = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var instructionText: TextView
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        recyclerView = findViewById(R.id.recyclerView)
        instructionText = findViewById(R.id.instructionText)
        resetButton = findViewById(R.id.resetButton)

        val exerciseList = listOf(
            Exercise(name = "Sentadillas", durationInSeconds = 60, videoId = "squats_video", repGoal = 10),
            Exercise(name = "Flexiones", durationInSeconds = 90, videoId = "pushups_video", repGoal = 15),
            Exercise(name = "Plancha", durationInSeconds = 60, videoId = "plank_video", repGoal = 0),
            Exercise(name = "Saltos de Tijera", durationInSeconds = 50, videoId = "jumping_jacks_video", repGoal = 20)
        )
// Cambio realizado por Eduardo

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ExerciseAdapter(exerciseList) { selectedExercise ->
            sendRoutineConfigToWear(selectedExercise)
            sendStartCommandToTV(selectedExercise)
            prepareForSmartStart()
        }

        // ¡NUEVA LÓGICA! El botón ahora es el "Botón Maestro"
        resetButton.setOnClickListener {
            Log.d("FitHome-Mobile", "Botón de Finalizar pulsado. Reiniciando todo.")
            // Enviamos las órdenes de parada
            sendStopCommandToWear()
            sendStopCommandToTV()
            // Y reiniciamos nuestra propia UI
            resetToSelectionScreen()
        }
    }

    private fun prepareForSmartStart() {
        isRoutineActive = true
        recyclerView.visibility = View.GONE
        instructionText.visibility = View.VISIBLE
        resetButton.visibility = View.VISIBLE // ¡Mostramos el botón de Finalizar!
        instructionText.text = "¡Rutina enviada!\n\nColoca el teléfono boca abajo para empezar."
    }

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

    private fun resetToSelectionScreen() {
        instructionText.visibility = View.GONE
        resetButton.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        findViewById<TextView>(R.id.textView).text = "Elige otra rutina"
    }

    // ¡NUEVA FUNCIÓN! Para decirle al reloj que se detenga y se cierre
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

    // --- El resto de funciones se quedan casi igual ---
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onResume() {
        super.onResume()
        lightSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
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