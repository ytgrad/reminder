package com.shiv.reminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiv.reminder.ReminderRepository
import com.shiv.reminder.db.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository): ViewModel(){

    fun getReminders(): LiveData<List<Reminder>>{
        return repository.getReminders()
    }

    fun addReminders(reminder: Reminder){
        viewModelScope.launch(Dispatchers.IO){
            repository.addReminders(reminder)
        }
    }

    fun updateReminder(reminder: Reminder){
        viewModelScope.launch(Dispatchers.IO){
            repository.updateReminder(reminder)
        }
    }

    fun deleteReminder(reminderId: Int){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteReminder(reminderId)
        }
    }

}