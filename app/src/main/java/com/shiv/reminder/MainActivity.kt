package com.shiv.reminder

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.CalendarContract.Colors
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shiv.reminder.adapters.ReminderAdapter
import com.shiv.reminder.databinding.ActivityMainBinding
import com.shiv.reminder.databinding.AddReminderDialogBinding
import com.shiv.reminder.db.Reminder
import com.shiv.reminder.db.ReminderDatabase
import com.shiv.reminder.viewmodels.ReminderViewModel
import com.shiv.reminder.viewmodels.ReminderViewModelFactory
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ReminderViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val reminderDatabase = ReminderDatabase.getDatabaseInstance(this)
        val reminderDao = reminderDatabase.reminderDao()
        val repository = ReminderRepository(reminderDao)
        viewModel = ViewModelProvider(this, ReminderViewModelFactory(repository))[ReminderViewModel::class.java]

        ////////////////////////////////////  recycler view
        val adapter = ReminderAdapter()
        binding.apply {
            rvReminderList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvReminderList.hasFixedSize()
            rvReminderList.adapter = adapter
        }
        viewModel.getReminders().observe(this, Observer {
            adapter.submitList(it)
        })

        binding.btnNewReminder.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog(){
        val dialogBinding = AddReminderDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))




        dialog.show()
    }

}