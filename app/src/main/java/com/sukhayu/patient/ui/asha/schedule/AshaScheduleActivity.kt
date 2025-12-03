package com.sukhayu.patient.ui.asha.schedule

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.local.entity.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AshaScheduleActivity : AppCompatActivity() {

    private lateinit var btnWeekly: Button
    private lateinit var btnMonthly: Button
    private lateinit var btnYearly: Button
    private lateinit var btnAddTask: Button
    private lateinit var calendarView: android.widget.CalendarView
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvTaskCount: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: TaskAdapter

    private var selectedView = ViewType.WEEKLY
    private var selectedDate: Long = 0L
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_schedule)

        supportActionBar?.apply {
            title = "My Schedule"
            setDisplayHomeAsUpEnabled(true)
        }

        initializeViews()
        setupRecyclerView()
        setupTabSwitching()
        setupCalendarListener()
        setupAddTaskButton()

        // Initialize predefined tasks
        initializePredefinedTasks()

        // Set initial selected date to today
        selectedDate = getStartOfDay(calendar.timeInMillis)
        loadTasksForSelectedDate()
    }

    private fun initializeViews() {
        btnWeekly = findViewById(R.id.btnWeekly)
        btnMonthly = findViewById(R.id.btnMonthly)
        btnYearly = findViewById(R.id.btnYearly)
        btnAddTask = findViewById(R.id.btnAddTask)
        calendarView = findViewById(R.id.calendarView)
        rvTasks = findViewById(R.id.rvTasks)
        tvTaskCount = findViewById(R.id.tvTaskCount)
        tvEmptyState = findViewById(R.id.tvEmptyState)
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter { task, isDone ->
            updateTaskStatus(task, isDone)
        }
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter
    }

    private fun setupTabSwitching() {
        btnWeekly.setOnClickListener { selectTab(ViewType.WEEKLY) }
        btnMonthly.setOnClickListener { selectTab(ViewType.MONTHLY) }
        btnYearly.setOnClickListener { selectTab(ViewType.YEARLY) }

        selectTab(ViewType.WEEKLY)
    }

    private fun setupCalendarListener() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedDate = calendar.timeInMillis
            loadTasksForSelectedDate()
        }
    }

    private fun setupAddTaskButton() {
        btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun selectTab(viewType: ViewType) {
        selectedView = viewType

        btnWeekly.isSelected = viewType == ViewType.WEEKLY
        btnMonthly.isSelected = viewType == ViewType.MONTHLY
        btnYearly.isSelected = viewType == ViewType.YEARLY

        updateButtonBackground(btnWeekly, viewType == ViewType.WEEKLY)
        updateButtonBackground(btnMonthly, viewType == ViewType.MONTHLY)
        updateButtonBackground(btnYearly, viewType == ViewType.YEARLY)

        loadTasksBasedOnView()
    }

    private fun updateButtonBackground(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(resources.getColor(android.R.color.holo_purple, theme))
            button.setTextColor(resources.getColor(android.R.color.white, theme))
        } else {
            button.setBackgroundColor(resources.getColor(android.R.color.white, theme))
            button.setTextColor(resources.getColor(android.R.color.black, theme))
        }
    }

    private fun loadTasksBasedOnView() {
        when (selectedView) {
            ViewType.WEEKLY -> loadWeeklyTasks()
            ViewType.MONTHLY -> loadMonthlyTasks()
            ViewType.YEARLY -> loadYearlyTasks()
        }
    }

    private fun loadWeeklyTasks() {
        lifecycleScope.launch {
            val tasks = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()

                // Get start and end of current week
                val weekStart = getStartOfWeek(calendar.timeInMillis)
                val weekEnd = getEndOfWeek(calendar.timeInMillis)

                dao.getTasksForDateRange(weekStart, weekEnd)
            }
            displayTasks(tasks)
        }
    }

    private fun loadMonthlyTasks() {
        lifecycleScope.launch {
            val tasks = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()

                // Get start and end of current month
                val monthStart = getStartOfMonth(calendar.timeInMillis)
                val monthEnd = getEndOfMonth(calendar.timeInMillis)

                dao.getTasksForDateRange(monthStart, monthEnd)
            }
            displayTasks(tasks)
        }
    }

    private fun loadYearlyTasks() {
        lifecycleScope.launch {
            val tasks = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()

                // Get start and end of current year
                val yearStart = getStartOfYear(calendar.timeInMillis)
                val yearEnd = getEndOfYear(calendar.timeInMillis)

                dao.getTasksForDateRange(yearStart, yearEnd)
            }
            displayTasks(tasks)
        }
    }

    private fun loadTasksForSelectedDate() {
        lifecycleScope.launch {
            val tasks = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()
                dao.getTasksForDate(selectedDate)
            }
            displayTasks(tasks)
        }
    }

    private fun displayTasks(tasks: List<TaskEntity>) {
        // Sort tasks so newest appear first (by createdAt descending)
        val sortedTasks = tasks.sortedByDescending { it.createdAt }
        adapter.submitList(sortedTasks)
        tvTaskCount.text = "${sortedTasks.size} task${if (sortedTasks.size != 1) "s" else ""}"

        if (sortedTasks.isEmpty()) {
            tvEmptyState.visibility = android.view.View.VISIBLE
            rvTasks.visibility = android.view.View.GONE
        } else {
            tvEmptyState.visibility = android.view.View.GONE
            rvTasks.visibility = android.view.View.VISIBLE
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTaskTitle = dialogView.findViewById<EditText>(R.id.etTaskTitle)

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTaskTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    addTask(title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addTask(title: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()
                val task = TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    date = selectedDate,
                    category = "custom",
                    isDone = false
                )
                dao.insertTask(task)
            }
            loadTasksForSelectedDate()
        }
    }

    private fun updateTaskStatus(task: TaskEntity, isDone: Boolean) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()
                dao.updateTask(task.copy(isDone = isDone))
            }
            // Reload to reflect changes
            when (selectedView) {
                ViewType.WEEKLY -> loadWeeklyTasks()
                ViewType.MONTHLY -> loadMonthlyTasks()
                ViewType.YEARLY -> loadYearlyTasks()
            }
        }
    }

    private fun initializePredefinedTasks() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).taskDao()
                val existingTasks = dao.getAllTasks()

                // Only initialize if no tasks exist
                if (existingTasks.isEmpty()) {
                    val predefinedTasks = mutableListOf<TaskEntity>()
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                    // Weekly tasks - add for current week
                    val weeklyTasks = listOf(
                        "Patient Home Visits",
                        "Community Health Awareness",
                        "Update Patient Records",
                        "Medicine Distribution"
                    )

                    val weekStart = getStartOfWeek(calendar.timeInMillis)
                    for (i in 0..6) {
                        val dayDate = weekStart + (i * 24 * 60 * 60 * 1000L)
                        weeklyTasks.forEach { taskTitle ->
                            predefinedTasks.add(
                                TaskEntity(
                                    id = UUID.randomUUID().toString(),
                                    title = taskTitle,
                                    date = dayDate,
                                    category = "weekly",
                                    isDone = false
                                )
                            )
                        }
                    }

                    // Monthly tasks - add for current month
                    val monthlyTasks = listOf(
                        "Monthly Health Camp",
                        "Immunization Drive",
                        "Nutrition Survey",
                        "Submit Monthly Report"
                    )

                    val monthStart = getStartOfMonth(calendar.timeInMillis)
                    monthlyTasks.forEachIndexed { index, taskTitle ->
                        predefinedTasks.add(
                            TaskEntity(
                                id = UUID.randomUUID().toString(),
                                title = taskTitle,
                                date = monthStart + (index * 7 * 24 * 60 * 60 * 1000L), // Spread across month
                                category = "monthly",
                                isDone = false
                            )
                        )
                    }

                    // Yearly tasks - fixed dates
                    // Leprosy Survey: 30 January
                    val jan30 = Calendar.getInstance().apply {
                        set(currentYear, Calendar.JANUARY, 30, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    predefinedTasks.add(
                        TaskEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Leprosy Awareness Survey",
                            date = jan30,
                            category = "yearly",
                            isDone = false,
                            description = "Mandatory leprosy survey as per national health guidelines"
                        )
                    )

                    // Leprosy Survey: 2 October
                    val oct2 = Calendar.getInstance().apply {
                        set(currentYear, Calendar.OCTOBER, 2, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    predefinedTasks.add(
                        TaskEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Leprosy Awareness Survey",
                            date = oct2,
                            category = "yearly",
                            isDone = false,
                            description = "Mandatory leprosy survey as per national health guidelines"
                        )
                    )

                    // Additional yearly events
                    val yearlyEvents = listOf(
                        Triple("World Health Day", Calendar.APRIL, 7),
                        Triple("World TB Day", Calendar.MARCH, 24),
                        Triple("National Immunization Day", Calendar.MARCH, 16)
                    )

                    yearlyEvents.forEach { (title, month, day) ->
                        val eventDate = Calendar.getInstance().apply {
                            set(currentYear, month, day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        predefinedTasks.add(
                            TaskEntity(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                date = eventDate,
                                category = "yearly",
                                isDone = false
                            )
                        )
                    }

                    dao.insertTasks(predefinedTasks)
                }
            }
        }
    }

    // Helper functions for date calculations
    private fun getStartOfDay(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getStartOfWeek(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfWeek(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = getStartOfWeek(timestamp)
            add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    private fun getStartOfMonth(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfMonth(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = getStartOfMonth(timestamp)
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    private fun getStartOfYear(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfYear(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = getStartOfYear(timestamp)
            add(Calendar.YEAR, 1)
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    enum class ViewType {
        WEEKLY, MONTHLY, YEARLY
    }
}

