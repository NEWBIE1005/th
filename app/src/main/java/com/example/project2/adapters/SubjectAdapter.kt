package com.example.project2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.databinding.ItemSubjectBinding
import com.example.project2.entities.Subject

class SubjectAdapter(
    private var list: List<Subject> = emptyList(),
    private val onItemClick: (Subject) -> Unit,
    private val onItemLongClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    fun updateData(newList: List<Subject>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = list[position]
        with(holder.binding) {
            tvCodeName.text = "${subject.subjectCode} - ${subject.subjectName}"
            tvCredits.text = "${subject.credits} tín chỉ"
            tvDescription.text = subject.description

            root.setOnClickListener { onItemClick(subject) }
            root.setOnLongClickListener {
                onItemLongClick(subject)
                true
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
