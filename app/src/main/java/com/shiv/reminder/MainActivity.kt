package com.shiv.reminder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.shiv.reminder.db.ReminderDatabase
import com.shiv.reminder.viewmodel.ReminderViewModel
import com.shiv.reminder.viewmodel.ReminderViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: ReminderViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val reminderDatabase = ReminderDatabase.getDatabaseInstance(this)
        val reminderDao = reminderDatabase.reminderDao()
        val repository = ReminderRepository(reminderDao)
        viewModel = ViewModelProvider(this, ReminderViewModelFactory(repository))[ReminderViewModel::class.java]




    }
}