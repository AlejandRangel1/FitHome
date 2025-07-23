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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.gms.wearable.Wearable
class ChronometerActivity : ComponentActivity(), SensorEventListener {

    // --- UI y Temporizador ---
    private lateinit var timerText: TextView
    private lateinit var titleText: TextView
    private lateinit var toggleButton: Button
    private lateinit var repCounterText: TextView

    private var secondsRemaining = 0
    private var isRunning = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    // --- Sensores y Lógica de Ejercicio ---
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var repCount = 0
    private var repGoal = 0
    private var isAtBottom = false
    private val SQUAT_THRESHOLD_DOWN = 6.0f
    private val SQUAT_THRESHOLD_UP = 9.0f

    // El receptor sigue aquí, pero ahora su trabajo es más simple
    private val startReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.fithome.START_TIMER") {
                Log.d("Routine", "Comando de inicio recibido. Simulando clic en el botón.")
                // Si la rutina no está corriendo, simulamos un clic en el botón de empezar.
                if (!isRunning) {
                    toggleButton.performClick()
                }
            }
        }
    }

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

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.fithome.STOP_ROUTINE") {
                Log.d("Routine", "Cerrando por orden del móvil.")
                finish() // Simplemente cerramos la actividad
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routineData = intent.getStringExtra("ROUTINE_DATA")?.split(";")
        val routineName = routineData?.getOrNull(0) ?: "Rutina"
        val totalSeconds = routineData?.getOrNull(1)?.toIntOrNull() ?: 60
        repGoal = routineData?.getOrNull(2)?.toIntOrNull() ?: 10
        secondsRemaining = totalSeconds

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        titleText = TextView(this).apply {
            text = routineName
            textSize = 20f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }

        repCounterText = TextView(this).apply {
            text = "Reps: $repCount / $repGoal"
            textSize = 28f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 20)
        }

        timerText = TextView(this).apply {
            textSize = 40f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
        updateTimerText()

        // ¡VOLVEMOS A LA LÓGICA DEL BOTÓN QUE SÍ FUNCIONABA!
        toggleButton = Button(this).apply {
            text = "Empezar"
            setOnClickListener {
                isRunning = !isRunning
                if (isRunning) {
                    text = "Pausar"
                    handler.post(runnable)
                    Log.d("Routine", "Rutina INICIADA/REANUDADA.")
                } else {
                    text = "Reanudar"
                    handler.removeCallbacks(runnable)
                    Log.d("Routine", "Rutina PAUSADA.")
                }
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(16, 16, 16, 16)
            addView(titleText)
            addView(repCounterText)
            addView(timerText)
            addView(toggleButton)
        }
        setContentView(layout)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER || !isRunning) return

        val y = event.values[1]

        if (!isAtBottom && y < SQUAT_THRESHOLD_DOWN) {
            isAtBottom = true
        }

        if (isAtBottom && y > SQUAT_THRESHOLD_UP) {
            repCount++
            repCounterText.text = "Reps: $repCount / $repGoal"
            isAtBottom = false
            Log.d("SquatDetector", "¡Repetición #$repCount detectada!")

            if (repGoal > 0 && repCount >= repGoal) {
                finishRoutine()
            }
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
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        }
        // Registramos el receptor para el inicio automático
        registerReceiver(startReceiver, IntentFilter("com.example.fithome.START_TIMER"), RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        // Desregistramos el receptor
        unregisterReceiver(startReceiver)
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