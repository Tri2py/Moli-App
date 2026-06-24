package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

// Glassmorphism Color Definitions
object GlassTheme {
    val darkBackground = Color(0x0A0A0A0A) // We use solid #0A0A0A with glow drawing in screen
    val lightBackground = Color(0xFFF8FAFC)

    fun getSurfaceColor(isDark: Boolean): Color {
        return if (isDark) Color(0x12FFFFFF) else Color(0xA6FFFFFF) // 7% white vs 65% white
    }

    fun getBorderColor(isDark: Boolean): Color {
        return if (isDark) Color(0x1FFFFFFF) else Color(0xCCFFFFFF) // 12% white vs 80% white
    }

    fun getTextColor(isDark: Boolean): Color {
        return if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    }

    fun getSubtextColor(isDark: Boolean): Color {
        return if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
    }

    val Accent = Color(0xFF4F46E5) // Clean premium indigo accent
    val AccentGlow = Color(0x264F46E5)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    cornerRadius: Dp = 28.dp,
    onClick: (() -> Unit)? = null,
    borderStroke: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundColor = GlassTheme.getSurfaceColor(isDark)
    val borderColor = GlassTheme.getBorderColor(isDark)
    val shape = RoundedCornerShape(cornerRadius)

    val baseModifier = modifier
        .shadow(
            elevation = if (isDark) 4.dp else 2.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.05f),
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
        .clip(shape)
        .background(backgroundColor)
        .then(
            if (borderStroke != null) {
                Modifier.border(borderStroke, shape)
            } else {
                Modifier.border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(borderColor, borderColor.copy(alpha = 0.3f))
                    ),
                    shape = shape
                )
            }
        )

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(
            onClick = onClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        )
    } else {
        baseModifier
    }

    Column(
        modifier = finalModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)

    val buttonModifier = modifier
        .height(48.dp)
        .clip(shape)
        .then(
            if (isPrimary) {
                Modifier.background(GlassTheme.Accent)
            } else {
                Modifier.background(GlassTheme.getSurfaceColor(isDark))
            }
        )
        .then(
            if (!isPrimary) {
                Modifier.border(
                    width = 1.dp,
                    color = GlassTheme.getBorderColor(isDark),
                    shape = shape
                )
            } else {
                Modifier
            }
        )
        .then(
            if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
        .clickable(
            onClick = onClick,
            interactionSource = interactionSource,
            indication = LocalIndication.current
        )

    Row(
        modifier = buttonModifier.padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) Color.White else GlassTheme.getTextColor(isDark),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (isPrimary) Color.White else GlassTheme.getTextColor(isDark),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GlassModal(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    isDark: Boolean = true,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {}, // Prevent clicks through card
                cornerRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = GlassTheme.getTextColor(isDark),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close Dialog",
                            tint = GlassTheme.getSubtextColor(isDark),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    isDark: Boolean = true,
    title: String,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.4f)
            )
        },
        containerColor = if (isDark) Color(0xED161616) else Color(0xFAFCFDFF), // High density frosted glass backing
        tonalElevation = 0.dp,
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = GlassTheme.getTextColor(isDark),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = GlassTheme.getSubtextColor(isDark)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun GlassNavigationBar(
    items: List<NavigationTabItem>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val navShape = RoundedCornerShape(32.dp)
    val backgroundColor = GlassTheme.getSurfaceColor(isDark)
    val borderColor = GlassTheme.getBorderColor(isDark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(
                elevation = 8.dp,
                shape = navShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(navShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.3f))
                ),
                shape = navShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == index
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(durationMillis = 150), label = ""
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
                )

                Column(
                    modifier = Modifier
                        .testTag("nav_tab_${item.tag}")
                        .clickable(
                            onClick = { onItemSelected(index) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassTheme.AccentGlow)
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                            contentDescription = item.label,
                            tint = if (isSelected) GlassTheme.Accent else GlassTheme.getTextColor(isDark).copy(alpha = alpha),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) GlassTheme.Accent else GlassTheme.getTextColor(isDark).copy(alpha = alpha),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class NavigationTabItem(
    val label: String,
    val tag: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

@Composable
fun GlassTaskCard(
    title: String,
    description: String,
    category: String,
    priority: String,
    dueDate: Long,
    isCompleted: Boolean,
    onCompleteChanged: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    testTag: String = ""
) {
    val strikeAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.5f else 1f,
        animationSpec = tween(150), label = ""
    )

    val formattedDate = remember(dueDate) {
        if (dueDate == 0L) "No date" else {
            val date = Date(dueDate)
            val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            format.format(date)
        }
    }

    val priorityColor = when (priority) {
        "High" -> Color(0xFFEF4444)
        "Medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val priorityBg = priorityColor.copy(alpha = 0.15f)

    GlassCard(
        isDark = isDark,
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Elegant Frosted Checkbox with dynamic selection animation
            Box(
                modifier = Modifier
                    .testTag("${testTag}_checkbox")
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) GlassTheme.Accent else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isCompleted) GlassTheme.Accent else GlassTheme.getSubtextColor(isDark).copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onCompleteChanged(!isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = GlassTheme.getTextColor(isDark).copy(alpha = strikeAlpha),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = GlassTheme.getSubtextColor(isDark).copy(alpha = strikeAlpha),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GlassTheme.getSurfaceColor(isDark).copy(alpha = 0.1f))
                            .border(0.5.dp, GlassTheme.getBorderColor(isDark).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = category,
                            color = GlassTheme.getTextColor(isDark).copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priority,
                            color = priorityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Date Icon + Label
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "Due Date",
                            tint = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDate,
                            color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .testTag("${testTag}_delete")
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete Task",
                    tint = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun GlassDateDisplayWidget(
    modifier: Modifier = Modifier,
    isDark: Boolean = true
) {
    val calendar = remember { Calendar.getInstance() }
    val dayNumber = remember { SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time) }
    val dayOfWeek = remember { SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time) }
    val monthAndYear = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time) }

    GlassCard(
        isDark = isDark,
        cornerRadius = 28.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Frosted Icon Container representing calendar day
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassTheme.getSurfaceColor(isDark).copy(alpha = if (isDark) 0.15f else 0.4f))
                    .border(1.dp, GlassTheme.getBorderColor(isDark).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = dayOfWeek.take(3).uppercase(),
                        color = GlassTheme.Accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = dayNumber,
                        color = GlassTheme.getTextColor(isDark),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dayOfWeek,
                    color = GlassTheme.getTextColor(isDark),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = monthAndYear,
                    color = GlassTheme.getSubtextColor(isDark),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Minimalist status/active syncing dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0x0FFFFFFF) else Color(0x0D000000))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)) // Emerald green status
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Active",
                    color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun getLucideCategoryIcon(category: String): ImageVector {
    return when (category.trim().lowercase()) {
        "gym" -> Icons.Outlined.FitnessCenter
        "work" -> Icons.Outlined.WorkOutline
        "eat" -> Icons.Outlined.Restaurant
        "focus" -> Icons.Outlined.Timer
        "design" -> Icons.Outlined.Palette
        "engineering" -> Icons.Outlined.Code
        "planning" -> Icons.Outlined.Assignment
        "personal" -> Icons.Outlined.Person
        else -> Icons.Outlined.TaskAlt
    }
}

@Composable
fun GlassUpcomingTaskCard(
    title: String,
    category: String,
    dueDate: Long,
    isCompleted: Boolean,
    onCompleteChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    testTag: String = ""
) {
    val lucideIcon = remember(category) { getLucideCategoryIcon(category) }
    val accentColor = remember(category) {
        when (category.trim().lowercase()) {
            "gym" -> Color(0xFF10B981) // Emerald
            "work" -> Color(0xFF3B82F6) // Blue
            "eat" -> Color(0xFFEF4444) // Red
            "focus" -> Color(0xFFF59E0B) // Amber
            "design" -> Color(0xFFEC4899) // Pink
            "engineering" -> Color(0xFF8B5CF6) // Purple
            "planning" -> Color(0xFF06B6D4) // Cyan
            else -> GlassTheme.Accent
        }
    }

    val strikeAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.5f else 1f,
        animationSpec = tween(150), label = ""
    )

    val formattedTime = remember(dueDate) {
        if (dueDate == 0L) "No time set" else {
            val date = Date(dueDate)
            val format = SimpleDateFormat("h:mm a • MMM d", Locale.getDefault())
            format.format(date)
        }
    }

    GlassCard(
        isDark = isDark,
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded icon container resembling a modern Lucide React component
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = lucideIcon,
                    contentDescription = "$category Icon",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Task Name and Category Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = GlassTheme.getTextColor(isDark).copy(alpha = strikeAlpha),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(3.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        color = accentColor.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    
                    Text(
                        text = "•",
                        color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    
                    Text(
                        text = formattedTime,
                        color = GlassTheme.getSubtextColor(isDark).copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Sleek minimalist complete checkbox
            Box(
                modifier = Modifier
                    .testTag("${testTag}_checkbox")
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) GlassTheme.Accent else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isCompleted) GlassTheme.Accent else GlassTheme.getSubtextColor(isDark).copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onCompleteChanged(!isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}


