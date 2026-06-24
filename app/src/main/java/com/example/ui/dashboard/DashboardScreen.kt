package com.example.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.data.Task
import com.example.ui.theme.FunlandFontFamily
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TaskStats
import com.example.ui.TaskViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()

    // Alarm states
    val isRinging by viewModel.isRinging.collectAsState()
    val activeRingingTask by viewModel.activeRingingTask.collectAsState()

    // Timer States
    val timeRemaining by viewModel.focusTimeRemaining.collectAsState()
    val isTimerRunning by viewModel.isFocusTimerRunning.collectAsState()
    val timerMode by viewModel.focusTimerMode.collectAsState()
    val totalTime by viewModel.focusTotalTime.collectAsState()

    // Modal Task Creation State
    var showAddTaskModal by remember { mutableStateOf(false) }

    // Splash/Start Loader Screen states
    var isSplashLoading by remember { mutableStateOf(true) }
    var renderSplash by remember { mutableStateOf(true) }
    val splashAlpha by animateFloatAsState(
        targetValue = if (isSplashLoading) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        finishedListener = { renderSplash = false },
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        delay(2200) // Premium 2.2-second intro loader
        isSplashLoading = false
    }

    // Screen Layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0A0A0A) else Color(0xFFF8FAFC))
    ) {
        // Glowing Backing Orbs (Custom Canvas) for the Frosted Glassmorphic look
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (isDark) {
                drawCircle(
                    color = Color(0x264F46E5), // Indigo #4F46E5 with 15% opacity
                    radius = 420.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.1f)
                )
                drawCircle(
                    color = Color(0x193B82F6), // Blue-500 with 10% opacity
                    radius = 380.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.9f)
                )
            } else {
                drawCircle(
                    color = Color(0x194F46E5), // Soft Indigo for Light Mode
                    radius = 420.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.1f, y = size.height * 0.1f)
                )
                drawCircle(
                    color = Color(0x123B82F6), // Soft Blue for Light Mode
                    radius = 380.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x = size.width * 0.9f, y = size.height * 0.9f)
                )
            }
        }

        // Main Scaffold Core Layout
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopAppBar(
                    isDark = isDark,
                    onThemeToggle = { viewModel.toggleTheme() }
                )
            },
            bottomBar = {
                val navItems = listOf(
                    NavigationTabItem("Overview", "overview", Icons.Outlined.Dashboard, Icons.Outlined.Dashboard),
                    NavigationTabItem("Workspace", "workspace", Icons.Outlined.WorkOutline, Icons.Outlined.WorkOutline),
                    NavigationTabItem("Focus", "focus", Icons.Outlined.Timer, Icons.Outlined.Timer)
                )
                GlassNavigationBar(
                    items = navItems,
                    selectedItem = selectedTab,
                    onItemSelected = { viewModel.setTab(it) },
                    isDark = isDark
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> OverviewTab(
                        stats = stats,
                        tasks = tasks,
                        isDark = isDark,
                        timeRemaining = timeRemaining,
                        isTimerRunning = isTimerRunning,
                        timerMode = timerMode,
                        totalTime = totalTime,
                        onTimerClick = { viewModel.setTab(2) },
                        onCompleteChanged = { viewModel.toggleTaskCompletion(it) },
                        onDeleteClick = { viewModel.deleteTask(it) }
                    )
                    1 -> WorkspaceTab(
                        tasks = tasks,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        selectedPriority = selectedPriority,
                        isDark = isDark,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setCategoryFilter(it) },
                        onPrioritySelect = { viewModel.setPriorityFilter(it) },
                        onCompleteChanged = { viewModel.toggleTaskCompletion(it) },
                        onDeleteClick = { viewModel.deleteTask(it) },
                        onAddTaskClick = { showAddTaskModal = true }
                    )
                    2 -> FocusTab(
                        timeRemaining = timeRemaining,
                        isTimerRunning = isTimerRunning,
                        timerMode = timerMode,
                        totalTime = totalTime,
                        isDark = isDark,
                        onStartTimer = { viewModel.startFocusTimer() },
                        onPauseTimer = { viewModel.pauseFocusTimer() },
                        onResetTimer = { viewModel.resetFocusTimer() },
                        onModeSelect = { viewModel.setTimerMode(it) }
                    )
                }
            }
        }

        // Create Task Modal Dialog
        if (showAddTaskModal) {
            GlassAddTaskModal(
                isDark = isDark,
                onDismissRequest = { showAddTaskModal = false },
                onTaskCreated = { title, desc, cat, prio, dueTimestamp, isAlarmEnabled ->
                    viewModel.addTask(title, desc, cat, prio, dueTimestamp, isAlarmEnabled)
                    showAddTaskModal = false
                }
            )
        }

        // Immersive In-App Ringing Dialog Overlay
        if (isRinging && activeRingingTask != null) {
            Dialog(
                onDismissRequest = { viewModel.dismissActiveAlarm() },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xB3000000)) // Deep premium semi-transparent backdrop
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "ringing_pulses")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )
                    val opacity by infiniteTransition.animateFloat(
                        initialValue = 0.15f,
                        targetValue = 0.55f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_opacity"
                    )

                    // Glowing Pulsing Orbs
                    Box(
                        modifier = Modifier
                            .size(340.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        GlassTheme.Accent.copy(alpha = opacity),
                                        Color.Transparent
                                    )
                                )
                            )
                            .scale(scale)
                    )

                    // Frosted Glass Alarm Dialog
                    GlassCard(
                        isDark = isDark,
                        cornerRadius = 32.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 380.dp)
                            .shadow(24.dp, RoundedCornerShape(32.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            // Ringing Icon
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.AccentGlow)
                                    .border(1.dp, GlassTheme.Accent.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Ringing Reminder",
                                    tint = GlassTheme.Accent,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .graphicsLayer {
                                            // Dynamic oscillating vibration animation
                                            rotationZ = (scale - 1.05f) * 45f
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "REMINDER RINGING",
                                color = GlassTheme.Accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.5.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = activeRingingTask?.title ?: "Alarm",
                                color = GlassTheme.getTextColor(isDark),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Category: ${activeRingingTask?.category}",
                                color = GlassTheme.getSubtextColor(isDark),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = activeRingingTask?.description ?: "",
                                color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Large Dismiss Button
                            GlassButton(
                                text = "Dismiss Alarm",
                                onClick = { viewModel.dismissActiveAlarm() },
                                isDark = isDark,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Immersive Glassmorphic Start Loader / Splash Overlay
        if (renderSplash) {
            GlassSplashScreen(
                isDark = isDark,
                alpha = splashAlpha
            )
        }
    }
}

@Composable
fun GlassTopAppBar(
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DAILY MOLI",
                color = GlassTheme.getTextColor(isDark),
                fontSize = 24.sp, // Slightly increased for bubbly display visibility
                fontWeight = FontWeight.Normal, // Bubbly display fonts look awesome at Normal/Bold
                letterSpacing = 1.sp,
                fontFamily = FunlandFontFamily
            )
            Text(
                text = "Workspace Sync",
                color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Theme Switcher Button with Glassmorphic styling
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GlassTheme.getSurfaceColor(isDark))
                .border(
                    width = 1.dp,
                    color = GlassTheme.getBorderColor(isDark),
                    shape = CircleShape
                )
                .clickable { onThemeToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = "Toggle Theme",
                tint = GlassTheme.getTextColor(isDark),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun OverviewTab(
    stats: TaskStats,
    tasks: List<Task>,
    isDark: Boolean,
    timeRemaining: Int,
    isTimerRunning: Boolean,
    timerMode: String,
    totalTime: Int,
    onTimerClick: () -> Unit,
    onCompleteChanged: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit
) {
    val activeTasks = remember(tasks) { 
        tasks.filter { !it.isCompleted }
            .sortedBy { it.dueDate }
            .take(4) 
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Current Date Widget
        item {
            val greeting = remember {
                val cal = Calendar.getInstance()
                when (cal.get(Calendar.HOUR_OF_DAY)) {
                    in 5..11 -> "Good morning"
                    in 12..17 -> "Good afternoon"
                    else -> "Good evening"
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(
                        text = "OVERVIEW",
                        color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = greeting,
                        color = GlassTheme.getTextColor(isDark),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                }
                
                GlassDateDisplayWidget(isDark = isDark)
            }
        }

        // Productivity Statistics Widget
        item {
            GlassCard(
                isDark = isDark,
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "METRICS",
                            color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Daily Completion",
                            color = GlassTheme.getTextColor(isDark),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.completedTasks} of ${stats.totalTasks} completed items.",
                            color = GlassTheme.getSubtextColor(isDark),
                            fontSize = 13.sp
                        )

                        if (stats.urgentTasksCount > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stats.urgentTasksCount} Urgent tasks outstanding",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Minimal Circular Progress Component
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        val strokeWidth = 8.dp
                        Canvas(modifier = Modifier.size(80.dp)) {
                            // Track
                            drawCircle(
                                color = if (isDark) Color(0x1AFFFFFF) else Color(0x1F000000),
                                style = Stroke(width = strokeWidth.toPx())
                            )
                            // Progress
                            val sweepAngle = (stats.completionRate.toFloat() / 100f) * 360f
                            drawArc(
                                color = GlassTheme.Accent,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${stats.completionRate}%",
                            color = GlassTheme.getTextColor(isDark),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Focus Widget Card (Shortcut to Focus tab)
        item {
            GlassCard(
                isDark = isDark,
                onClick = onTimerClick,
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isTimerRunning) GlassTheme.AccentGlow else GlassTheme.getSurfaceColor(isDark)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Focus",
                            tint = if (isTimerRunning) GlassTheme.Accent else GlassTheme.getTextColor(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FOCUS MODE",
                            color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isTimerRunning) "Active Focus Session" else "Start Focus Session",
                            color = GlassTheme.getTextColor(isDark),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$timerMode • ${formatTime(timeRemaining)} remaining",
                            color = GlassTheme.getSubtextColor(isDark),
                            fontSize = 13.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Navigate to Focus Timer",
                        tint = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Core Calendar Strip Widget
        item {
            CalendarStripWidget(isDark = isDark)
        }

        // Active Tasks Preview Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPCOMING FOCUS",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (activeTasks.isEmpty()) {
            item {
                GlassCard(isDark = isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming tasks today.",
                            color = GlassTheme.getSubtextColor(isDark),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeTasks, key = { it.id }) { task ->
                GlassUpcomingTaskCard(
                    title = task.title,
                    category = task.category,
                    dueDate = task.dueDate,
                    isCompleted = task.isCompleted,
                    onCompleteChanged = { onCompleteChanged(task) },
                    isDark = isDark,
                    testTag = "task_overview_${task.id}"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CalendarStripWidget(isDark: Boolean) {
    val calendar = Calendar.getInstance()
    val todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-based index

    // Get calendar days for the current week
    val weekDays = remember {
        val days = mutableListOf<CalendarDay>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val sdfDay = SimpleDateFormat("dd", Locale.getDefault())
        val sdfName = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 0..6) {
            days.add(
                CalendarDay(
                    dayNumber = sdfDay.format(cal.time),
                    dayName = sdfName.format(cal.time).substring(0, 1),
                    isToday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH) &&
                            Calendar.getInstance().get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "WEEKLY SCHEDULE",
            color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { day ->
                val bgBrush = if (day.isToday) {
                    Brush.verticalGradient(
                        colors = listOf(GlassTheme.Accent, GlassTheme.Accent.copy(alpha = 0.8f))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (day.isToday) Modifier.background(bgBrush) else Modifier.background(GlassTheme.getSurfaceColor(isDark))
                        )
                        .border(
                            width = 1.dp,
                            color = if (day.isToday) Color.Transparent else GlassTheme.getBorderColor(isDark).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayName,
                            color = if (day.isToday) Color.White else GlassTheme.getSubtextColor(isDark),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayNumber,
                            color = if (day.isToday) Color.White else GlassTheme.getTextColor(isDark),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class CalendarDay(
    val dayNumber: String,
    val dayName: String,
    val isToday: Boolean
)

@Composable
fun WorkspaceTab(
    tasks: List<Task>,
    searchQuery: String,
    selectedCategory: String,
    selectedPriority: String,
    isDark: Boolean,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onPrioritySelect: (String) -> Unit,
    onCompleteChanged: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onAddTaskClick: () -> Unit
) {
    val categories = listOf("All", "Gym", "Work", "Eat", "Focus", "Design", "Engineering", "Planning")
    val priorities = listOf("All", "High", "Medium", "Low")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Search Input Widget Styled as Glassmorphism Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(14.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(14.dp))
                .background(GlassTheme.getSurfaceColor(isDark))
                .border(
                    width = 1.dp,
                    color = GlassTheme.getBorderColor(isDark),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "Search tasks...",
                            color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = GlassTheme.getTextColor(isDark),
                        unfocusedTextColor = GlassTheme.getTextColor(isDark)
                    ),
                    modifier = Modifier
                        .testTag("search_input")
                        .fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Category Badges Strip
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .testTag("filter_cat_$category")
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) GlassTheme.Accent else GlassTheme.getSurfaceColor(isDark)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else GlassTheme.getBorderColor(isDark).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onCategorySelect(category) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else GlassTheme.getTextColor(isDark),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        // Priority Badges Strip
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(priorities) { priority ->
                val isSelected = selectedPriority == priority
                Box(
                    modifier = Modifier
                        .testTag("filter_prio_$priority")
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) GlassTheme.Accent.copy(alpha = 0.15f) else GlassTheme.getSurfaceColor(isDark).copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) GlassTheme.Accent.copy(alpha = 0.4f) else GlassTheme.getBorderColor(isDark).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onPrioritySelect(priority) }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (priority == "All") "Priority: All" else priority,
                        color = if (isSelected) GlassTheme.Accent else GlassTheme.getTextColor(isDark).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Task Trigger Card
        GlassCard(
            isDark = isDark,
            onClick = onAddTaskClick,
            cornerRadius = 14.dp,
            modifier = Modifier
                .testTag("add_task_trigger")
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = GlassTheme.Accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sync New Task",
                    color = GlassTheme.Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Planner Tasks List
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.WorkOutline,
                        contentDescription = "Empty Workspace",
                        tint = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Clean Space",
                        color = GlassTheme.getTextColor(isDark).copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create a task to coordinate your day.",
                        color = GlassTheme.getSubtextColor(isDark),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    GlassTaskCard(
                        title = task.title,
                        description = task.description,
                        category = task.category,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        isCompleted = task.isCompleted,
                        onCompleteChanged = { onCompleteChanged(task) },
                        onDeleteClick = { onDeleteClick(task) },
                        isDark = isDark,
                        testTag = "task_item_${task.id}"
                    )
                }
            }
        }
    }
}

@Composable
fun FocusTab(
    timeRemaining: Int,
    isTimerRunning: Boolean,
    timerMode: String,
    totalTime: Int,
    isDark: Boolean,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onModeSelect: (String) -> Unit
) {
    val modes = listOf("Work", "Short Break", "Long Break")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FOCUS ZONE",
            color = GlassTheme.Accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Stay in flow.",
            color = GlassTheme.getTextColor(isDark),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Timer Dial
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Glow backdrop behind timer dial
            Canvas(modifier = Modifier.size(230.dp)) {
                drawCircle(
                    color = GlassTheme.AccentGlow.copy(alpha = if (isTimerRunning) 0.3f else 0.15f),
                    radius = size.minDimension / 2
                )
            }

            // Outer Glass Card circle
            GlassCard(
                isDark = isDark,
                cornerRadius = 120.dp,
                modifier = Modifier.size(210.dp)
            ) {
                // Circular Progress Indicator Ring
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val progressFraction = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 1f
                    val strokeWidth = 5.dp

                    Canvas(modifier = Modifier.size(170.dp)) {
                        drawCircle(
                            color = if (isDark) Color(0x10FFFFFF) else Color(0x0A000000),
                            style = Stroke(width = strokeWidth.toPx())
                        )
                        drawArc(
                            color = GlassTheme.Accent,
                            startAngle = -90f,
                            sweepAngle = progressFraction * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatTime(timeRemaining),
                            color = GlassTheme.getTextColor(isDark),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = timerMode.uppercase(),
                            color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mode segmented switcher styled with glass card
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(GlassTheme.getSurfaceColor(isDark))
                .border(
                    width = 1.dp,
                    color = GlassTheme.getBorderColor(isDark),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            modes.forEach { mode ->
                val isSelected = timerMode == mode
                Box(
                    modifier = Modifier
                        .testTag("timer_mode_$mode")
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GlassTheme.Accent else Color.Transparent)
                        .clickable { onModeSelect(mode) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mode,
                        color = if (isSelected) Color.White else GlassTheme.getTextColor(isDark),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Box(
                modifier = Modifier
                    .testTag("timer_reset_button")
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GlassTheme.getSurfaceColor(isDark))
                    .border(1.dp, GlassTheme.getBorderColor(isDark), CircleShape)
                    .clickable { onResetTimer() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = "Reset Timer",
                    tint = GlassTheme.getTextColor(isDark).copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Play/Pause Button
            Box(
                modifier = Modifier
                    .testTag("timer_play_pause_button")
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GlassTheme.Accent)
                    .clickable {
                        if (isTimerRunning) onPauseTimer() else onStartTimer()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTimerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isTimerRunning) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Bottom Sheet Form for Adding Task
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassAddTaskBottomSheet(
    isDark: Boolean,
    onDismissRequest: () -> Unit,
    onTaskCreated: (String, String, String, String, Long, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Gym") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDueDays by remember { mutableStateOf(0) } // 0: Today, 1: Tomorrow, -1: Now (10s Demo)
    var isAlarmEnabled by remember { mutableStateOf(true) }
    var alarmHour by remember { mutableStateOf(12) }
    var alarmMinute by remember { mutableStateOf(0) }

    val categories = listOf("Gym", "Work", "Eat", "Focus", "Design", "Engineering")
    val priorities = listOf("High", "Medium", "Low")
    val dueOptions = listOf(0 to "Today", 1 to "Tomorrow", -1 to "Now (10s)")

    GlassBottomSheet(
        onDismissRequest = onDismissRequest,
        isDark = isDark,
        title = "Sync New Task"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title", color = GlassTheme.getSubtextColor(isDark)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Accent,
                    unfocusedBorderColor = GlassTheme.getBorderColor(isDark),
                    focusedTextColor = GlassTheme.getTextColor(isDark),
                    unfocusedTextColor = GlassTheme.getTextColor(isDark)
                ),
                modifier = Modifier
                    .testTag("task_title_input")
                    .fillMaxWidth(),
                singleLine = true
            )

            // Task Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details", color = GlassTheme.getSubtextColor(isDark)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Accent,
                    unfocusedBorderColor = GlassTheme.getBorderColor(isDark),
                    focusedTextColor = GlassTheme.getTextColor(isDark),
                    unfocusedTextColor = GlassTheme.getTextColor(isDark)
                ),
                modifier = Modifier
                    .testTag("task_desc_input")
                    .fillMaxWidth(),
                maxLines = 3
            )

            // Category Segmented Selector
            Column {
                Text(
                    text = "CATEGORY",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .testTag("select_cat_$cat")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) GlassTheme.Accent else GlassTheme.getSurfaceColor(isDark)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else GlassTheme.getBorderColor(isDark),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else GlassTheme.getTextColor(isDark),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Priority Selection
            Column {
                Text(
                    text = "PRIORITY",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorities.forEach { prio ->
                        val isSelected = selectedPriority == prio
                        val color = when (prio) {
                            "High" -> Color(0xFFEF4444)
                            "Medium" -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                        Box(
                            modifier = Modifier
                                .testTag("select_prio_$prio")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) color.copy(alpha = 0.15f) else GlassTheme.getSurfaceColor(isDark)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) color else GlassTheme.getBorderColor(isDark),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPriority = prio }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio,
                                color = if (isSelected) color else GlassTheme.getTextColor(isDark),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Schedule / Due Date Selection
            Column {
                Text(
                    text = "DUE SCHEDULE",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dueOptions.forEach { option ->
                        val isSelected = selectedDueDays == option.first
                        Box(
                            modifier = Modifier
                                .testTag("select_due_${option.second}")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) GlassTheme.Accent else GlassTheme.getSurfaceColor(isDark)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else GlassTheme.getBorderColor(isDark),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDueDays = option.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.second,
                                color = if (isSelected) Color.White else GlassTheme.getTextColor(isDark),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Glassmorphic Digital Alarm Time Picker (Shown only if not doing immediate 10s demo)
            if (selectedDueDays != -1) {
                Column {
                    Text(
                        text = "ALARM TIME",
                        color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour Selector Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassTheme.getSurfaceColor(isDark))
                                .border(1.dp, GlassTheme.getBorderColor(isDark), RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { alarmHour = if (alarmHour == 0) 23 else alarmHour - 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", alarmHour),
                                color = GlassTheme.getTextColor(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { alarmHour = if (alarmHour == 23) 0 else alarmHour + 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(":", color = GlassTheme.getTextColor(isDark), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        // Minute Selector Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassTheme.getSurfaceColor(isDark))
                                .border(1.dp, GlassTheme.getBorderColor(isDark), RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { alarmMinute = if (alarmMinute == 0) 59 else alarmMinute - 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", alarmMinute),
                                color = GlassTheme.getTextColor(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { alarmMinute = if (alarmMinute == 59) 0 else alarmMinute + 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Toggle switch for alarm active
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ring Alarm",
                                color = GlassTheme.getSubtextColor(isDark),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = isAlarmEnabled,
                                onCheckedChange = { isAlarmEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GlassTheme.Accent,
                                    uncheckedThumbColor = GlassTheme.getSubtextColor(isDark),
                                    uncheckedTrackColor = GlassTheme.getSurfaceColor(isDark)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button
            GlassButton(
                text = "Sync to Workspace",
                onClick = {
                    if (title.isNotBlank()) {
                        val calendar = Calendar.getInstance().apply {
                            if (selectedDueDays == 1) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            set(Calendar.HOUR_OF_DAY, alarmHour)
                            set(Calendar.MINUTE, alarmMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dueTimestamp = if (selectedDueDays == -1) {
                            System.currentTimeMillis() + 10000L // 10 seconds demo!
                        } else {
                            calendar.timeInMillis
                        }
                        onTaskCreated(title, description, selectedCategory, selectedPriority, dueTimestamp, isAlarmEnabled)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isDark = isDark,
                icon = Icons.Outlined.CloudUpload,
                testTag = "create_task_button"
            )
        }
    }
}

// Date/Time Formatter Helper
fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

@Composable
fun GlassAddTaskModal(
    isDark: Boolean,
    onDismissRequest: () -> Unit,
    onTaskCreated: (String, String, String, String, Long, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Gym") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedDueDays by remember { mutableStateOf(0) } // 0: Today, 1: Tomorrow, -1: Now (10s Demo)
    var isAlarmEnabled by remember { mutableStateOf(true) }
    var alarmHour by remember { mutableStateOf(12) }
    var alarmMinute by remember { mutableStateOf(0) }

    val categories = listOf("Gym", "Work", "Eat", "Focus", "Design", "Engineering")
    val priorities = listOf("High", "Medium", "Low")
    val dueOptions = listOf(0 to "Today", 1 to "Tomorrow", -1 to "Now (10s)")

    val scrollState = rememberScrollState()

    GlassModal(
        onDismissRequest = onDismissRequest,
        isDark = isDark,
        title = "Sync New Task"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp) // Maintain compact dialog profile, fully scrollable
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title", color = GlassTheme.getSubtextColor(isDark)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Accent,
                    unfocusedBorderColor = GlassTheme.getBorderColor(isDark),
                    focusedTextColor = GlassTheme.getTextColor(isDark),
                    unfocusedTextColor = GlassTheme.getTextColor(isDark)
                ),
                modifier = Modifier
                    .testTag("task_title_input_modal")
                    .fillMaxWidth(),
                singleLine = true
            )

            // Task Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Details", color = GlassTheme.getSubtextColor(isDark)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Accent,
                    unfocusedBorderColor = GlassTheme.getBorderColor(isDark),
                    focusedTextColor = GlassTheme.getTextColor(isDark),
                    unfocusedTextColor = GlassTheme.getTextColor(isDark)
                ),
                modifier = Modifier
                    .testTag("task_desc_input_modal")
                    .fillMaxWidth(),
                maxLines = 3
            )

            // Category Selection Grid (3 columns per row) with Lucide-styled icons
            Column {
                Text(
                    text = "CATEGORY",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.chunked(3).forEach { rowCategories ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowCategories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                val icon = getLucideCategoryIcon(cat)
                                val accentColor = when (cat.trim().lowercase()) {
                                    "gym" -> Color(0xFF10B981) // Emerald
                                    "work" -> Color(0xFF3B82F6) // Blue
                                    "eat" -> Color(0xFFEF4444) // Red
                                    "focus" -> Color(0xFFF59E0B) // Amber
                                    "design" -> Color(0xFFEC4899) // Pink
                                    "engineering" -> Color(0xFF8B5CF6) // Purple
                                    else -> GlassTheme.Accent
                                }
                                Box(
                                    modifier = Modifier
                                        .testTag("modal_select_cat_$cat")
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) accentColor.copy(alpha = 0.15f) else GlassTheme.getSurfaceColor(isDark)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) accentColor else GlassTheme.getBorderColor(isDark),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) accentColor else GlassTheme.getSubtextColor(isDark),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = cat,
                                            color = if (isSelected) GlassTheme.getTextColor(isDark) else GlassTheme.getSubtextColor(isDark),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Priority Selection
            Column {
                Text(
                    text = "PRIORITY",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorities.forEach { prio ->
                        val isSelected = selectedPriority == prio
                        val color = when (prio) {
                            "High" -> Color(0xFFEF4444)
                            "Medium" -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                        Box(
                            modifier = Modifier
                                .testTag("modal_select_prio_$prio")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) color.copy(alpha = 0.15f) else GlassTheme.getSurfaceColor(isDark)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) color else GlassTheme.getBorderColor(isDark),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPriority = prio }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio,
                                color = if (isSelected) color else GlassTheme.getTextColor(isDark),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Schedule / Due Date Selection
            Column {
                Text(
                    text = "DUE SCHEDULE",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dueOptions.forEach { option ->
                        val isSelected = selectedDueDays == option.first
                        Box(
                            modifier = Modifier
                                .testTag("modal_select_due_${option.second}")
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) GlassTheme.Accent else GlassTheme.getSurfaceColor(isDark)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else GlassTheme.getBorderColor(isDark),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDueDays = option.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.second,
                                color = if (isSelected) Color.White else GlassTheme.getTextColor(isDark),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Glassmorphic Digital Alarm Time Picker (Shown only if not doing immediate 10s demo)
            if (selectedDueDays != -1) {
                Column {
                    Text(
                        text = "ALARM TIME",
                        color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour Selector Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassTheme.getSurfaceColor(isDark))
                                .border(1.dp, GlassTheme.getBorderColor(isDark), RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { alarmHour = if (alarmHour == 0) 23 else alarmHour - 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", alarmHour),
                                color = GlassTheme.getTextColor(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { alarmHour = if (alarmHour == 23) 0 else alarmHour + 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(":", color = GlassTheme.getTextColor(isDark), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        // Minute Selector Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlassTheme.getSurfaceColor(isDark))
                                .border(1.dp, GlassTheme.getBorderColor(isDark), RoundedCornerShape(8.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { alarmMinute = if (alarmMinute == 0) 59 else alarmMinute - 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("-", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%02d", alarmMinute),
                                color = GlassTheme.getTextColor(isDark),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = { alarmMinute = if (alarmMinute == 59) 0 else alarmMinute + 1 },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("+", color = GlassTheme.getTextColor(isDark), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Toggle switch for alarm active
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Ring Alarm",
                                color = GlassTheme.getSubtextColor(isDark),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = isAlarmEnabled,
                                onCheckedChange = { isAlarmEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GlassTheme.Accent,
                                    uncheckedThumbColor = GlassTheme.getSubtextColor(isDark),
                                    uncheckedTrackColor = GlassTheme.getSurfaceColor(isDark)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button
            GlassButton(
                text = "Sync to Workspace",
                onClick = {
                    if (title.isNotBlank()) {
                        val calendar = Calendar.getInstance().apply {
                            if (selectedDueDays == 1) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                            set(Calendar.HOUR_OF_DAY, alarmHour)
                            set(Calendar.MINUTE, alarmMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dueTimestamp = if (selectedDueDays == -1) {
                            System.currentTimeMillis() + 10000L // 10 seconds demo!
                        } else {
                            calendar.timeInMillis
                        }
                        onTaskCreated(title, description, selectedCategory, selectedPriority, dueTimestamp, isAlarmEnabled)
                    }
                },
                modifier = Modifier
                    .testTag("modal_create_task_button")
                    .fillMaxWidth(),
                isDark = isDark,
                icon = Icons.Outlined.CloudUpload
            )
        }
    }
}

@Composable
fun GlassSplashScreen(
    isDark: Boolean,
    alpha: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_loader")
    
    // Rotating loading ring angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse animation for the central icon
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Soft glowing aura alpha
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .background(if (isDark) Color(0xFF0A0A0A) else Color(0xFFF8FAFC)),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Backing Orbs for frosted feel
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF4F46E5).copy(alpha = auraAlpha), // Indigo accent
                radius = 300.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = size.width * 0.5f, y = size.height * 0.45f)
            )
            drawCircle(
                color = Color(0xFF3B82F6).copy(alpha = auraAlpha * 0.7f), // Blue
                radius = 240.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x = size.width * 0.5f, y = size.height * 0.45f)
            )
        }

        // Frosted Glass Content Panel
        Box(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(GlassTheme.getSurfaceColor(isDark))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassTheme.getBorderColor(isDark),
                            GlassTheme.getBorderColor(isDark).copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(vertical = 48.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Central Glowing Pulsing Logo Icon
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5).copy(alpha = 0.25f),
                                    Color(0xFF4F46E5).copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF4F46E5).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TaskAlt,
                        contentDescription = "Daily Moli Emblem",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Beautiful Display Typography Lockup
                Text(
                    text = "DAILY MOLI",
                    color = GlassTheme.getTextColor(isDark),
                    fontSize = 32.sp, // Slightly bigger for full display splash look
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.sp,
                    fontFamily = FunlandFontFamily,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SYNC YOUR DAY",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Rotating Minimalist Loading Circle
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotationAngle }) {
                        // Background track
                        drawCircle(
                            color = GlassTheme.getBorderColor(isDark).copy(alpha = 0.2f),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Glowing arc sweep
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF4F46E5).copy(alpha = 0.1f),
                                    Color(0xFF4F46E5),
                                    Color(0xFF3B82F6),
                                    Color(0xFF4F46E5).copy(alpha = 0.1f)
                                )
                            ),
                            startAngle = 0f,
                            sweepAngle = 280f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Initializing workspace...",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
