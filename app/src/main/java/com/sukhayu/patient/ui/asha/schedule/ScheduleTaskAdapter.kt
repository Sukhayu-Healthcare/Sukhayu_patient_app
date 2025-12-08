package com.sukhayu.patient.ui.asha.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class ScheduleTaskAdapter(
    private val items: MutableList<ScheduleTask>
) : RecyclerView.Adapter<ScheduleTaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvTaskDate)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbTaskDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = items[position]
        holder.tvTitle.text = task.title
        holder.tvDate.text = AshaScheduleActivity.formatDate(task.dateMillis)

        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.isDone

        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            task.isDone = isChecked
        }
    }

    override fun getItemCount(): Int = items.size
}
