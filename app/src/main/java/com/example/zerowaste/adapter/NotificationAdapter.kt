package com.example.zerowaste

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.zerowaste.data.remote.NotificationResponse
import com.example.zerowaste.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * The adapter for the RecyclerView. It efficiently manages the list of notifications
 * and binds the data to the views for each item.
 */
class NotificationAdapter(
    private val onItemClicked: (NotificationResponse) -> Unit
) : ListAdapter<NotificationResponse, NotificationAdapter.NotificationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
        holder.itemView.setOnClickListener { onItemClicked(notification) }
    }

    class NotificationViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

        fun bind(notification: NotificationResponse) {
            binding.tvMessage.text = notification.message
            binding.tvTimestamp.text = dateFormat.format(notification.createdAt)

            // Make unread notifications appear bold for better UX
            if (!notification.markAsRead) {
                binding.tvMessage.setTypeface(null, Typeface.BOLD)
                binding.tvTimestamp.setTypeface(null, Typeface.BOLD)
            } else {
                binding.tvMessage.setTypeface(null, Typeface.NORMAL)
                binding.tvTimestamp.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    companion object {
        // DiffUtil helps the adapter efficiently update only the items that have changed.
        private val DiffCallback = object : DiffUtil.ItemCallback<NotificationResponse>() {
            override fun areItemsTheSame(oldItem: NotificationResponse, newItem: NotificationResponse): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NotificationResponse, newItem: NotificationResponse): Boolean {
                return oldItem == newItem
            }
        }
    }
}