package com.example.fithome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ExerciseAdapter(
    private val exercises: List<Exercise>,
    private val onItemClicked: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    // Describe la vista de cada elemento y la guarda en un ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
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
        // Configura el click listener para toda la fila
        holder.itemView.setOnClickListener {
            onItemClicked(exercise)
        }
    }

    // Cambio realizado por Eduardo

    // Clase interna que contiene las vistas de cada una de las fila
    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.exercise_name)
        private val durationTextView: TextView = itemView.findViewById(R.id.exercise_duration)

        fun bind(exercise: Exercise) {
            nameTextView.text = exercise.name
            durationTextView.text = "Duración: ${exercise.durationInSeconds}s"
        }
    }
}