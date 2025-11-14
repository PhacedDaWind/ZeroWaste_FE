package com.example.zerowaste.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
// Import M3 components
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zerowaste.data.remote.NotificationResponse
import com.example.zerowaste.data.remote.NotificationType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // ⭐ STATE TO TRACK EXPANDED NOTIFICATION: Stores the ID of the expanded notification (or null)
    var expandedId by remember { mutableStateOf<Long?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                actions = {
                    IconButton(onClick = { viewModel.deleteAllNotifications() }) {
                        Icon(Icons.Default.DeleteSweep, "Delete All")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            NotificationFilterTabs(
                selectedType = uiState.filterType,
                onTabSelected = { viewModel.filterNotifications(it) }
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMessage!!, color = Color.Red, textAlign = TextAlign.Center)
                    }
                }
                uiState.notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No new notifications", textAlign = TextAlign.Center)
                    }
                }
                else -> {
                    NotificationList(
                        notifications = uiState.notifications,
                        expandedId = expandedId, // Pass the expanded state down
                        onNotificationClicked = { notification ->
                            // 1. Mark as read on click
                            viewModel.markAsRead(notification.id)

                            // 2. Toggle expansion state
                            expandedId = if (expandedId == notification.id) null else notification.id
                        },
                        onDeleteClicked = {
                            viewModel.deleteNotification(it.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationFilterTabs(
    selectedType: NotificationType?,
    onTabSelected: (NotificationType?) -> Unit
) {
    // Use the enums you provided
    val tabs = listOf(null, NotificationType.FOOD_INVENTORY_ALERT, NotificationType.DONATION_CLAIMED, NotificationType.MEAL_REMINDER)
    val tabTitles = listOf("All", "Inventory", "Donations", "Meals")

    TabRow(selectedTabIndex = tabs.indexOf(selectedType).coerceAtLeast(0)) {
        tabs.forEachIndexed { index, type ->
            Tab(
                selected = selectedType == type,
                onClick = { onTabSelected(type) },
                text = { Text(tabTitles[index]) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationList(
    notifications: List<NotificationResponse>,
    expandedId: Long?, // ⭐ New parameter
    onNotificationClicked: (NotificationResponse) -> Unit,
    onDeleteClicked: (NotificationResponse) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.StartToEnd ||
                        value == SwipeToDismissBoxValue.EndToStart
                    ) {
                        onDeleteClicked(notification)
                        true
                    } else false
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Red.copy(alpha = 0.6f))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                },
                content = {
                    NotificationItem(
                        notification = notification,
                        isExpanded = expandedId == notification.id, // Pass expansion status
                        onClick = { onNotificationClicked(notification) }
                    )
                }
            )

            HorizontalDivider()
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationResponse,
    isExpanded: Boolean, // ⭐ New parameter
    onClick: () -> Unit
) {
    // Unread items are bold
    val fontWeight = if (!notification.markAsRead) FontWeight.Bold else FontWeight.Normal
    val backgroundColor = if (!notification.markAsRead) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = notification.notifType.getIcon(),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Use the .title property from your model
            Text(
                text = notification.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))

            // ⭐ MODIFIED: Display full message if expanded, otherwise truncated
            if (isExpanded) {
                Text(
                    text = notification.message, // Full message
                    fontWeight = fontWeight,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = notification.message, // Short description (or full if not too long)
                    fontWeight = fontWeight,
                    maxLines = 1, // Truncate to one line
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = formatTimestamp(notification.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // ⭐ Optional: Add an expansion indicator icon
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand"
        )
    }
}

// Helper function to get an icon based on your NotificationType
private fun NotificationType.getIcon(): ImageVector {
    return when (this) {
        NotificationType.FOOD_INVENTORY_ALERT -> Icons.Default.Kitchen
        NotificationType.DONATION_POSTED -> Icons.Default.AddBox
        NotificationType.DONATION_CLAIMED -> Icons.Default.Redeem
        NotificationType.MEAL_REMINDER -> Icons.Default.RestaurantMenu
    }
}

// Helper function to format the date
private fun formatTimestamp(date: Date?): String {
    if (date == null) return ""
    // Format: "10:30 AM" or "Nov 07"
    val calendar = Calendar.getInstance()
    calendar.time = date
    val today = Calendar.getInstance()

    return if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    ) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}