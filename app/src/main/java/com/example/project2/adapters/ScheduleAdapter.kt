package com.example.project2.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.databinding.ItemScheduleBinding
import com.example.project2.entities.ScheduleView
import com.example.project2.utils.TimeUtils

class ScheduleAdapter(
    private var list: List<ScheduleView> = emptyList(),
    private val onItemClick: ((ScheduleView) -> Unit)? = null,
    private val onItemLongClick: ((ScheduleView) -> Unit)? = null
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    fun updateData(newList: List<ScheduleView>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvSubject.text = "${item.subjectCode} - ${item.subjectName}"
            val displayDate = TimeUtils.displayDate(item.schedule.studyDate)
            tvDateTime.text = "$displayDate · ${item.schedule.startTime} – ${item.schedule.endTime}"
            tvLocation.text = item.schedule.location
            tvStatus.text = item.schedule.status

            when (item.schedule.status) {
                "CONFIRMED" -> {
                    tvStatus.setTextColor(Color.parseColor("#2E7D32")) // Green
                }
                "CANCELLED" -> {
                    tvStatus.setTextColor(Color.parseColor("#757575")) // Grey
                }
                else -> { // PENDING
                    tvStatus.setTextColor(Color.parseColor("#E65100")) // Orange
                }
            }

            root.setOnClickListener {
                onItemClick?.invoke(item)
            }
            root.setOnLongClickListener {
                onItemLongClick?.invoke(item)
                onItemLongClick != null
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
