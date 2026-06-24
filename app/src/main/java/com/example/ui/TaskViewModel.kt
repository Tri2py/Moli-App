package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.data.AlarmScheduler
import com.example.data.AlarmController
import com.example.data.RingingTaskInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val _isDarkMode = MutableStateFlow(true) // Defaults to Dark Mode for the premium look
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Alarm state exposures
    val isRinging: StateFlow<Boolean> = AlarmController.isRinging
    val activeRingingTask: StateFlow<RingingTaskInfo?> = AlarmController.activeRingingTask

    fun dismissActiveAlarm() {
        AlarmController.stopRinging()
    }

    // Filter and Search States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedPriority = MutableStateFlow("All")
    val selectedPriority: StateFlow<String> = _selectedPriority.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Dashboard, 1: Tasks, 2: Focus
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Focus Timer States
    private val _focusTimeRemaining = MutableStateFlow(1500) // 25 mins
    val focusTimeRemaining: StateFlow<Int> = _focusTimeRemaining.asStateFlow()

    private val _isFocusTimerRunning = MutableStateFlow(false)
    val isFocusTimerRunning: StateFlow<Boolean> = _isFocusTimerRunning.asStateFlow()

    private val _focusTimerMode = MutableStateFlow("Work") // "Work", "Short Break", "Long Break"
    val focusTimerMode: StateFlow<String> = _focusTimerMode.asStateFlow()

    private val _focusTotalTime = MutableStateFlow(1500)
    val focusTotalTime: StateFlow<Int> = _focusTotalTime.asStateFlow()

    private var timerJob: Job? = null

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())

        // Seed initial high-quality, professional tasks if empty
        viewModelScope.launch {
            repository.allTasks.first().let { currentTasks ->
                if (currentTasks.isEmpty()) {
                    seedDefaultTasks()
                }
            }
        }
    }

    // Reactive task list combined with filters and search
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .combine(_searchQuery) { taskList, query ->
            if (query.isBlank()) taskList else {
                taskList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
            }
        }
        .combine(_selectedCategory) { taskList, cat ->
            if (cat == "All") taskList else taskList.filter { it.category == cat }
        }
        .combine(_selectedPriority) { taskList, prio ->
            if (prio == "All") taskList else taskList.filter { it.priority == prio }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic stats derived from the tasks list
    val stats = repository.allTasks.map { taskList ->
        val total = taskList.size
        val completed = taskList.count { it.isCompleted }
        val completionRate = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
        val highPriorityCount = taskList.count { !it.isCompleted && it.priority == "High" }
        TaskStats(total, completed, completionRate, highPriorityCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats(0, 0, 0, 0)
    )

    // Core Database Functions
    fun addTask(title: String, description: String, category: String, priority: String, dueDate: Long, isAlarmEnabled: Boolean = false) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                category = category,
                priority = priority,
                dueDate = dueDate,
                isAlarmEnabled = isAlarmEnabled
            )
            val id = repository.insert(task)
            if (isAlarmEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), task.copy(id = id.toInt()))
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.update(updatedTask)
            if (updatedTask.isCompleted) {
                AlarmScheduler.cancelAlarm(getApplication(), task)
            } else if (updatedTask.isAlarmEnabled) {
                AlarmScheduler.scheduleAlarm(getApplication(), updatedTask)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), task)
            repository.delete(task)
        }
    }

    // Settings Toggle
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Navigation state
    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setPriorityFilter(priority: String) {
        _selectedPriority.value = priority
    }

    // Focus Timer Controllers
    fun startFocusTimer() {
        if (_isFocusTimerRunning.value) return
        _isFocusTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_focusTimeRemaining.value > 0) {
                delay(1000)
                _focusTimeRemaining.value -= 1
            }
            onTimerFinished()
        }
    }

    fun pauseFocusTimer() {
        _isFocusTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        setTimerMode(_focusTimerMode.value)
    }

    fun setTimerMode(mode: String) {
        pauseFocusTimer()
        _focusTimerMode.value = mode
        val duration = when (mode) {
            "Work" -> 1500       // 25 mins
            "Short Break" -> 300 // 5 mins
            "Long Break" -> 900  // 15 mins
            else -> 1500
        }
        _focusTimeRemaining.value = duration
        _focusTotalTime.value = duration
    }

    private fun onTimerFinished() {
        pauseFocusTimer()
        // Simple automatic mode switcher
        if (_focusTimerMode.value == "Work") {
            setTimerMode("Short Break")
        } else {
            setTimerMode("Work")
        }
    }

    private suspend fun seedDefaultTasks() {
        val today = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        val defaultTasks = listOf(
            Task(
                title = "Evening Gym Workout",
                description = "Leg day and active stretching. Keep up the high energy!",
                category = "Gym",
                priority = "High",
                dueDate = today
            ),
            Task(
                title = "Deliver Workspace Report",
                description = "Finalize and align the project slides for the engineering presentation.",
                category = "Work",
                priority = "High",
                dueDate = today + oneDay
            ),
            Task(
                title = "Prepare Meal Prep",
                description = "Plan healthy protein-focused lunches and snacks for the week.",
                category = "Eat",
                priority = "Medium",
                dueDate = today + 2 * oneDay
            ),
            Task(
                title = "Weekly Review Focus Session",
                description = "Set objectives, review blockers, and sync with the core team.",
                category = "Focus",
                priority = "Low",
                dueDate = today + 3 * oneDay
            )
        )

        for (task in defaultTasks) {
            repository.insert(task)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // ViewModel Factory
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                return TaskViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class TaskStats(
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Int,
    val urgentTasksCount: Int
)
