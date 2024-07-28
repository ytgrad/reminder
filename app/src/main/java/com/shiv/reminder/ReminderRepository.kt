package com.shiv.reminder

import androidx.lifecycle.LiveData
import com.shiv.reminder.db.Reminder
import com.shiv.reminder.db.ReminderDao

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getReminders():LiveData<List<Reminder>>{
        return reminderDao.getReminders()
    }

    suspend fun addReminders(reminder: Reminder){
        reminderDao.addReminder(reminder)
    }

}