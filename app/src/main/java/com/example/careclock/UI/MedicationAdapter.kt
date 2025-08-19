// app/src/main/java/com/example/careclock/ui/MedicationAdapter.kt
package com.example.careclock.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.careclock.R
import com.example.careclock.models.Medication
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class MedicationAdapter(
    private val list: List<Medication>,
    private val listener: (Medication, Action) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.VH>() {

    enum class Action { EDIT, DELETE }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tvName)
        val next = itemView.findViewById<TextView>(R.id.tvNext)
        val countdown = itemView.findViewById<TextView>(R.id.tvCountdown)
        val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_medication, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val med = list[position]
        holder.name.text = med.name

        val nextMs = med.nextDoseAfter()
        if (nextMs == Long.MAX_VALUE) {
            holder.next.text = "Tratamento concluído"
            holder.countdown.text = "-"
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.next.text = "Próxima: ${sdf.format(Date(nextMs))}"

            // countdown simples em HH:mm:ss
            val diff = max(0L, nextMs - System.currentTimeMillis())
            val s = diff / 1000
            val h = s / 3600
            val m = (s % 3600) / 60
            val sec = s % 60
            holder.countdown.text = String.format("%02d:%02d:%02d", h, m, sec)
        }

        holder.itemView.setOnClickListener {
            listener(med, Action.EDIT)
        }
        holder.btnDelete.setOnClickListener {
            listener(med, Action.DELETE)
        }
    }
}