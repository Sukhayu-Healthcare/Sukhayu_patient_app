package com.sukhayu.patient.ui.asha.schedule

import android.app.AlertDialog
import android.os.Bundle
import android.widget.CalendarView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.sukhayu.patient.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ScheduleTask(
    val title: String,
    val dateMillis: Long,
    var isDone: Boolean = false
)

class AshaScheduleActivity : AppCompatActivity() {

    private lateinit var tabScheduleScope: TabLayout
    private lateinit var calendarView: CalendarView
    private lateinit var rvTasks: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton

    private val tasksForSelectedDay = mutableListOf<ScheduleTask>()
    private lateinit var adapter: ScheduleTaskAdapter

    private var selectedDateMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_schedule)

        tabScheduleScope = findViewById(R.id.tabScheduleScope)
        calendarView = findViewById(R.id.calendarView)
        rvTasks = findViewById(R.id.rvTasks)
        fabAddTask = findViewById(R.id.fabAddTask)

        setupRecyclerView()
        setupCalendar()
        setupTabs()
        setupFab()

        // default tab: Weekly
        loadPredefinedTasksForTab("Weekly")
    }

    private fun setupRecyclerView() {
        adapter = ScheduleTaskAdapter(tasksForSelectedDay)
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter
    }

    private fun setupCalendar() {
        selectedDateMillis = calendarView.date

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            selectedDateMillis = cal.timeInMillis

            // If later you add Room, load tasks here for this date.
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupTabs() {
        tabScheduleScope.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val label = tab?.text?.toString() ?: "Weekly"
                loadPredefinedTasksForTab(label)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                val label = tab?.text?.toString() ?: "Weekly"
                loadPredefinedTasksForTab(label)
            }
        })
    }

    private fun setupFab() {
        fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val editText = EditText(this)
        editText.hint = "Task title"

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(editText)
            .setPositiveButton("Add") { dialog, _ ->
                val title = editText.text.toString().trim()
                if (title.isNotEmpty()) {
                    // New task should appear at the TOP
                    val newTask = ScheduleTask(
                        title = title,
                        dateMillis = selectedDateMillis,
                        isDone = false
                    )
                    tasksForSelectedDay.add(0, newTask)
                    adapter.notifyDataSetChanged()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadPredefinedTasksForTab(tabLabel: String) {
        tasksForSelectedDay.clear()

        when (tabLabel) {
            "Weekly" -> {
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "VHND / immunization follow-up",
                        dateMillis = selectedDateMillis
                    )
                )
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Home visits for high-risk families",
                        dateMillis = selectedDateMillis
                    )
                )
            }

            "Monthly" -> {
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Monthly ASHA meeting at PHC",
                        dateMillis = selectedDateMillis
                    )
                )
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Update household register & monthly report",
                        dateMillis = selectedDateMillis
                    )
                )
            }

            "Yearly" -> {
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Leprosy awareness & survey campaign day",
                        dateMillis = selectedDateMillis
                    )
                )
            }
        }

        adapter.notifyDataSetChanged()
    }

    companion object {
        fun formatDate(millis: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }
    }
}
