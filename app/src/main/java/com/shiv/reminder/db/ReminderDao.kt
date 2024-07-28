package com.shiv.reminder.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReminderDao {
    
    @Insert
    suspend fun addReminder(reminder: Reminder)
    
    @Query("SELECT * FROM reminders_table")
    fun getReminders(): LiveData<List<Reminder>>
    
}