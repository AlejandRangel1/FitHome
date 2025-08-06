package com.example.fithome.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.fithome.R // Asegúrate de que este import existe y no está en gris
import com.google.android.gms.wearable.Wearable

class ChronometerActivity : ComponentActivity(), SensorEventListener {

    // --- Variables de la UI ---
    private lateinit var timerText: TextView
    private lateinit var titleText: TextView
    private lateinit var toggleButton: Button
    private lateinit var repCounterText: TextView

    // --- Variables de Lógica ---
    private var secondsRemaining = 0
    private var isRunning = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var repCount = 0
    private var repGoal = 0
    private var isAtBottom = false
    private val SQUAT_THRESHOLD_DOWN = 6.0f
    private val SQUAT_THRESHOLD_UP = 9.0f

    // --- Receptores de Comunicación ---
    private val startReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.fithome.START_TIMER") {
                Log.d("Routine", "Comando de inicio recibido. Simulando clic.")
                if (!isRunning) { toggleButton.performClick() }
            }
        }
    }
    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.fithome.STOP_ROUTINE") {
                Log.d("Routine", "Cerrando por orden del móvil.")
                finish()
            }
        }
    }

    // --- Lógica del Temporizador ---
    private val runnable = object : Runnable {
        override fun run() {
            if (isRunning && secondsRemaining > 0) {
                secondsRemaining--
                updateTimerText()
                handler.postDelayed(this, 1000)
            } else if (secondsRemaining == 0 && isRunning) {
                finishRoutine()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. ¡ESTA LÍNEA ES LA MÁS IMPORTANTE DE TODAS!
        // Le dice a la actividad que use el diseño del archivo XML.
        setContentView(R.layout.activity_chronometer)

        // 2. BORRA TODO el código antiguo que creaba TextViews y Buttons aquí.
        // Ahora, conectamos las variables a las vistas que existen en el XML.
        timerText = findViewById(R.id.timerText)
        titleText = findViewById(R.id.titleText)
        toggleButton = findViewById(R.id.toggleButton)
        repCounterText = findViewById(R.id.repCounterText)

        // --- El resto de la lógica se queda igual ---
        val routineData = intent.getStringExtra("ROUTINE_DATA")?.split(";")
        val routineName = routineData?.getOrNull(0) ?: "Rutina"
        val totalSeconds = routineData?.getOrNull(1)?.toIntOrNull() ?: 60
        repGoal = routineData?.getOrNull(2)?.toIntOrNull() ?: 10
        secondsRemaining = totalSeconds

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

// Asignamos los datos iniciales a las vistas

// Esto está bien si routineName es dinámico
        titleText.text = routineName

// Solución intermedia: usar String.format() para evitar concatenación directa
        repCounterText.text = String.format("Reps: %d / %d", repCount, repGoal)

// Actualiza el texto del temporizador
        updateTimerText()



        // Asignamos la lógica al botón del XML
        toggleButton.setOnClickListener {
            isRunning = !isRunning
            if (isRunning) {
                toggleButton.text = "Pausar"
                handler.post(runnable)
                Log.d("Routine", "Rutina INICIADA/REANUDADA.")
            } else {
                toggleButton.text = "Reanudar"
                handler.removeCallbacks(runnable)
                Log.d("Routine", "Rutina PAUSADA.")
            }
        }
    }

    // --- El resto de la clase no necesita cambios ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER || !isRunning) return
        val y = event.values[1]
        if (!isAtBottom && y < SQUAT_THRESHOLD_DOWN) { isAtBottom = true }
        if (isAtBottom && y > SQUAT_THRESHOLD_UP) {
            repCount++
            repCounterText.text = "Reps: $repCount / $repGoal"
            isAtBottom = false
            Log.d("SquatDetector", "¡Repetición #$repCount detectada!")
            if (repGoal > 0 && repCount >= repGoal) { finishRoutine() }
        }
    }

    private fun sendMessageToMobile(path: String, data: ByteArray?) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(this).sendMessage(node.id, path, data)
                    .addOnSuccessListener { Log.d("FitHome-Wear", "Mensaje '$path' enviado a ${node.displayName}") }
                    .addOnFailureListener { Log.e("FitHome-Wear", "Error al enviar mensaje '$path'", it) }
            }
        }
    }

    private fun finishRoutine() {
        if (!isRunning) return
        isRunning = false
        handler.removeCallbacks(runnable)
        timerText.text = "¡Logrado!"
        toggleButton.text = "Finalizado"
        toggleButton.isEnabled = false
        Log.d("Routine", "¡Rutina finalizada!")
        sendMessageToMobile("/routine_finished", null)
        handler.postDelayed({ finish() }, 2000)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        registerReceiver(startReceiver, IntentFilter("com.example.fithome.START_TIMER"), RECEIVER_NOT_EXPORTED)
        registerReceiver(stopReceiver, IntentFilter("com.example.fithome.STOP_ROUTINE"), RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        unregisterReceiver(startReceiver)
        unregisterReceiver(stopReceiver)
    }

    private fun updateTimerText() {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}