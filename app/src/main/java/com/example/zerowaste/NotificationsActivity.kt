package com.example.zerowaste

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zerowaste.databinding.ActivityNotificationsBinding
import com.example.zerowaste.viewmodel.NotificationViewModel

/**
 * This is the main screen for displaying notifications. It observes the ViewModel
 * for data and updates the UI accordingly.
 */
class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    private var currentUserId: Long = -1L

    companion object {
        // A constant key for passing the user ID to this activity.
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the logged-in user's ID passed from the previous activity (e.g., HomeActivity).
        currentUserId = intent.getLongExtra(EXTRA_USER_ID, -1L)

        if (currentUserId == -1L) {
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if no user ID is found.
            return
        }

        setupRecyclerView()
        observeViewModel()

        // Trigger the initial fetch of notifications for the current user.
        viewModel.fetchNotifications(currentUserId)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            // When a notification item is clicked, mark it as read if it isn't already.
            if (!notification.markAsRead) {
                viewModel.markNotificationAsRead(notification.id, currentUserId)
            }
        }
        binding.recyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
        }
    }

    private fun observeViewModel() {
        // Observe changes in the list of notifications.
        viewModel.notifications.observe(this) { notificationList ->
            notificationAdapter.submitList(notificationList)
        }

        // Observe the loading state to show/hide the progress bar.
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe for errors and display them as a toast message.
        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
}