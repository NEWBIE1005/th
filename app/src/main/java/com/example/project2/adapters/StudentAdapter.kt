package com.example.project2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project2.databinding.ItemStudentBinding
import com.example.project2.entities.Student

class StudentAdapter(
    private var list: List<Student> = emptyList(),
    private val onItemClick: (Student) -> Unit,
    private val onItemLongClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    fun updateData(newList: List<Student>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = list[position]
        with(holder.binding) {
            tvCodeName.text = "${student.studentCode} - ${student.fullname}"
            tvContact.text = "${student.email} · ${student.phone}"

            root.setOnClickListener { onItemClick(student) }
            root.setOnLongClickListener {
                onItemLongClick(student)
                true
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
