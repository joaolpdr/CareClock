package com.example.careclock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.careclock.R

// O adapter agora aceita uma função "listener" que será chamada no clique
class DependentAdapter(
    private val dependents: List<Map<String, Any>>,
    private val onItemClicked: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<DependentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvDependentName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dependent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dependent = dependents[position]
        holder.nameTextView.text = dependent["name"] as? String ?: "Nome inválido"

        // Adiciona o listener de clique ao item
        holder.itemView.setOnClickListener {
            onItemClicked(dependent)
        }
    }

    override fun getItemCount() = dependents.size
}