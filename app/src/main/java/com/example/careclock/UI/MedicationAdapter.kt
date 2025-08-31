package com.example.careclock.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.careclock.R
import com.example.careclock.models.Medication
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max


class MedicationAdapter(
    var list: List<Medication>,
    private val listener: (Medication, Action) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.VH>() {

    enum class Action { EDIT, DELETE }
    private lateinit var context: Context

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvName)
        val countdown: TextView = itemView.findViewById(R.id.tvCountdown)
        val startTime: TextView = itemView.findViewById(R.id.tvStartTime)
        val endDateLabel: TextView = itemView.findViewById(R.id.tvEndDateLabel)
        val layout: ConstraintLayout = itemView as ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        context = parent.context
        val v = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val med = list[position]
        holder.name.text = med.name

        if (position % 2 == 0) {
            holder.layout.background.setTint(ContextCompat.getColor(context, R.color.teal_700))
        } else {
            holder.layout.background.setTint(ContextCompat.getColor(context, R.color.item_red))
        }

        med.durationDays?.let { days ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = med.startTimeMillis
            calendar.add(Calendar.DAY_OF_YEAR, days)
            val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
            holder.endDateLabel.text = "Termina em ${sdf.format(calendar.time)}"
            holder.endDateLabel.visibility = View.VISIBLE
        } ?: run {
            holder.endDateLabel.text = "Duração: Indeterminado" // Texto alternativo
            holder.endDateLabel.visibility = View.VISIBLE
        }

        val nextMs = med.nextDoseAfter()
        if (nextMs == Long.MAX_VALUE) {
            holder.countdown.text = "Concluído"
            holder.startTime.text = "Tratamento finalizado"
        } else {
            val diff = max(0L, nextMs - System.currentTimeMillis())
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            holder.countdown.text = String.format("%d horas e %d minutos", hours, minutes)

            val startDiff = max(0L, System.currentTimeMillis() - med.startTimeMillis)
            val days = TimeUnit.MILLISECONDS.toDays(startDiff)
            val startHours = TimeUnit.MILLISECONDS.toHours(startDiff) % 24
            holder.startTime.text = String.format("iniciou o tratamento a: %d dias e %d horas", days, startHours)
        }

        // Ação de clique curto para EDITAR
        holder.itemView.setOnClickListener {
            listener(med, Action.EDIT)
        }

        // AÇÃO DE CLIQUE LONGO PARA APAGAR
        holder.itemView.setOnLongClickListener {
            listener(med, Action.DELETE)
            true // Retorna true para indicar que o evento foi consumido
        }
    }
}