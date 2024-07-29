package com.shiv.reminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.shiv.reminder.databinding.ItemViewBinding
import com.shiv.reminder.db.Reminder

class ReminderAdapter: ListAdapter<Reminder, ReminderAdapter.MyViewHolder>(ReminderDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyViewHolder(private val view: ItemViewBinding): ViewHolder(view.root){
        fun bind(item: Reminder){
            view.apply {
                tvTime.text = item.time.toString().slice(0..4)
                tvDate.text = item.date.toString().slice(5..9)
                tvYear.text = "2024"
                tvTitle.text = item.title
                tvDesc.text = item.description
            }
        }
    }

    class ReminderDiffUtil: DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}