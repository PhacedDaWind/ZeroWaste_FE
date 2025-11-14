package com.example.zerowaste.ui.notification

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zerowaste.R
import com.example.zerowaste.data.remote.NotificationResponse

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = viewModel() // 1. REMOVED the userId parameter
) {
    val uiState by viewModel.uiState.collectAsState()

    // 2. This LaunchedEffect now triggers the initial data load
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold(
        bottomBar = {
            BottomActionBar(
                // 3. Updated to call the ViewModel without passing userId
                onMostRecent = { viewModel.fetchNotifications(unreadOnly = false) },
                onRemoveUnread = { viewModel.fetchNotifications(unreadOnly = true) }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Notifications",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error ?: "", color = Color.Red)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkRead = { viewModel.markNotificationAsRead(notification) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationCard(notification: NotificationResponse, onMarkRead: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkRead() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.markAsRead) Color.White else Color(0xFFE3F2FD)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Image Placeholder
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, shape = CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BottomActionBar(
    onMostRecent: () -> Unit,
    onRemoveUnread: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onMostRecent,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5))
        ) {
            Text("Most Recent")
        }

        Button(
            onClick = onRemoveUnread,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5))
        ) {
            Text("Remove Unread")
        }
    }
}
