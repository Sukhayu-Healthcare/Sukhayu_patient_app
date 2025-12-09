package com.sukhayu.patient.ui.asha.schedule

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
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
    private lateinit var btnWriteNotes: Button
    private lateinit var btnHandbook: ImageButton

    private val tasksForSelectedDay = mutableListOf<ScheduleTask>()
    private lateinit var adapter: ScheduleTaskAdapter

    private var selectedDateMillis: Long = System.currentTimeMillis()

    // In-memory map for daily notes keyed by dateMillis
    private val dailyNotes = mutableMapOf<Long, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_schedule)

        tabScheduleScope = findViewById(R.id.tabScheduleScope)
        calendarView = findViewById(R.id.calendarView)
        rvTasks = findViewById(R.id.rvTasks)
        fabAddTask = findViewById(R.id.fabAddTask)
        btnWriteNotes = findViewById(R.id.btnWriteNotes)
        btnHandbook = findViewById(R.id.btnHandbook)

        setupRecyclerView()
        setupCalendar()
        setupTabs()
        setupFab()
        setupNotesButton()
        setupHandbookButton()

        // Default tab: Daily
        loadPredefinedTasksForTab("Daily")
        tabScheduleScope.selectTab(tabScheduleScope.getTabAt(0))
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

    private fun setupNotesButton() {
        btnWriteNotes.setOnClickListener {
            showNotesDialog()
        }
    }

    private fun setupHandbookButton() {
        btnHandbook.setOnClickListener {
            val intent = Intent(this, AshaHandbookActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showNotesDialog() {
        val editText = EditText(this)
        editText.hint = "Write your notes here"
        editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        editText.minLines = 5
        editText.maxLines = 8

        // Load existing note for this date if available
        val existingNote = dailyNotes[selectedDateMillis]
        if (existingNote != null) {
            editText.setText(existingNote)
        }

        AlertDialog.Builder(this)
            .setTitle("Notes for ${formatDate(selectedDateMillis)}")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val noteText = editText.text.toString().trim()
                if (noteText.isNotEmpty()) {
                    dailyNotes[selectedDateMillis] = noteText
                } else {
                    dailyNotes.remove(selectedDateMillis)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
            "Daily" -> {
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Home visits as per line list",
                        dateMillis = selectedDateMillis
                    )
                )
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Record services in daily diary/register",
                        dateMillis = selectedDateMillis
                    )
                )
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Follow-up of high-risk mothers and newborns",
                        dateMillis = selectedDateMillis
                    )
                )
                tasksForSelectedDay.add(
                    ScheduleTask(
                        title = "Counseling for pregnant women and eligible couples",
                        dateMillis = selectedDateMillis
                    )
                )
            }

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
