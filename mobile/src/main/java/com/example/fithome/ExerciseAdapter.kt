package com.example.fithome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onItemClicked: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    // Describe la vista de cada elemento y la guarda en un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        // Apuntamos al nuevo layout 'list_item_exercise.xml' que diseñamos
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    // Devuelve la cantidad total de elementos en la lista
    override fun getItemCount(): Int {
        return exercises.size
    }

    // Toma los datos de una posición y los muestra en el ViewHolder
    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
        // Configura el click listener para toda la tarjeta
        holder.itemView.setOnClickListener {
            onItemClicked(exercise)
        }
    }

<<<<<<< Updated upstream
    // Cambio realizado por Eduardo

    // Clase interna que contiene las vistas de cada una de las fila
=======
    // Clase interna que contiene las vistas de cada fila (ahora con ImageView)
>>>>>>> Stashed changes
    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 1. Obtenemos las referencias a las vistas del nuevo layout
        private val nameTextView: TextView = itemView.findViewById(R.id.exercise_name)
        private val durationTextView: TextView = itemView.findViewById(R.id.exercise_duration)
        private val imageView: ImageView = itemView.findViewById(R.id.exercise_image)

        fun bind(exercise: Exercise) {
            // 2. Asignamos los datos a las vistas
            nameTextView.text = exercise.name

            // Mejoramos el texto para mostrar también las repeticiones
            if (exercise.repGoal > 0) {
                durationTextView.text = "Duración: ${exercise.durationInSeconds}s  •  Reps: ${exercise.repGoal}"
            } else {
                durationTextView.text = "Duración: ${exercise.durationInSeconds}s"
            }

            // 3. Lógica simple para poner un icono diferente por ejercicio
            // ¡IMPORTANTE! Necesitarás tener estos archivos de imagen en tu carpeta res/drawable
            // Por ejemplo: ic_squats.png, ic_pushups.png, ic_plank.png, ic_jumping_jacks.png
            when {
                exercise.name.contains("Sentadillas", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.ic_squats)
                }
                exercise.name.contains("Flexiones", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.ic_pushups)
                }
                exercise.name.contains("Plancha", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.ic_plank)
                }
                exercise.name.contains("Saltos", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.ic_jumping_jacks)
                }
                exercise.name.contains("Zancadas", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.zancadas)
                }
                exercise.name.contains("Burpees", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.burpees)
                }
                exercise.name.contains("Mountain", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.mountain_climbers)
                }
                exercise.name.contains("Skipping", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.skipping)
                }
                exercise.name.contains("Puente", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.puente_gluteos)
                }
                exercise.name.contains("Elevaciones de Pierna", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.elevaciones_pierna)
                }
                exercise.name.contains("Abdominales", ignoreCase = true) -> {
                    imageView.setImageResource(R.drawable.abdominales)
                }
                else -> {
                    // Si no encuentra una imagen específica, puedes poner una por defecto
                    // imageView.setImageResource(R.drawable.ic_default_exercise)
                }
            }
        }
    }
}