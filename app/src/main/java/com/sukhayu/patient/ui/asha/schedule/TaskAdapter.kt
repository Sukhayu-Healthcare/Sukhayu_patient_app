package com.sukhayu.patient.ui.asha.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onTaskChecked: (TaskEntity, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val items = mutableListOf<TaskEntity>()

    fun submitList(tasks: List<TaskEntity>) {
        items.clear()
        items.addAll(tasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        private val tvTaskDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        private val cbTaskDone: CheckBox = itemView.findViewById(R.id.cbTaskDone)

        fun bind(task: TaskEntity) {
            tvTaskTitle.text = task.title

            // Format date as "December 4, 2025"
            val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            tvTaskDate.text = dateFormat.format(Date(task.date))

            // Set checkbox state without triggering listener
            cbTaskDone.setOnCheckedChangeListener(null)
            cbTaskDone.isChecked = task.isDone

            // Set new listener
            cbTaskDone.setOnCheckedChangeListener { _, isChecked ->
                onTaskChecked(task, isChecked)
            }

            // Apply strikethrough if done
            if (task.isDone) {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvTaskTitle.alpha = 0.5f
                tvTaskDate.alpha = 0.5f
            } else {
                tvTaskTitle.paintFlags = tvTaskTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTaskTitle.alpha = 1.0f
                tvTaskDate.alpha = 1.0f
            }
        }
    }
}

