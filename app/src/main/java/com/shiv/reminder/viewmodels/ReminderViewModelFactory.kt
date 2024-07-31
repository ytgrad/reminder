package com.shiv.reminder.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shiv.reminder.ReminderRepository

class ReminderViewModelFactory(private val repository: ReminderRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderViewModel(repository) as T
    }
}