package com.shiv.reminder.adapters

import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.shiv.reminder.MainActivity
import com.shiv.reminder.ReminderRepository
import com.shiv.reminder.databinding.ItemViewBinding
import com.shiv.reminder.db.Reminder

class ReminderAdapter(private val listener: RecyclerViewEvent): ListAdapter<Reminder, ReminderAdapter.MyViewHolder>(ReminderDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun deleteReminderOnSlide(position: Int){
        listener.onItemClick(position)
    }

    inner class MyViewHolder(private val view: ItemViewBinding): ViewHolder(view.root), View.OnLongClickListener{

        init {
            view.root.setOnLongClickListener(this)
        }

        fun bind(item: Reminder){
            val amOrPm = if (item.time.hour > 11) "pm" else "am"
            val hourToShow = if (item.time.hour > 12) item.time.hour - 12 else item.time.hour
            view.apply {
                tvTime.text = String.format("%02d:%02d %s", hourToShow, item.time.minute, amOrPm)
                tvDate.text = "${item.date.dayOfMonth} ${item.date.month.toString().slice(0..2).toLowerCase().replaceFirstChar { a -> a.uppercase() }}"
                tvYear.text = item.date.year.toString()
                tvTitle.text = item.title
                tvDesc.text = item.description
            }

        }

        override fun onLongClick(p0: View?): Boolean {
            listener.onItemLongClick(adapterPosition)
            return true
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

    interface RecyclerViewEvent{
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }
}