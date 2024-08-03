package com.shiv.reminder

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.shiv.reminder.adapters.ReminderAdapter
import com.shiv.reminder.databinding.ActivityMainBinding
import com.shiv.reminder.databinding.AddReminderDialogBinding
import com.shiv.reminder.db.Reminder
import com.shiv.reminder.db.ReminderDatabase
import com.shiv.reminder.viewmodels.ReminderViewModel
import com.shiv.reminder.viewmodels.ReminderViewModelFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), ReminderAdapter.RecyclerViewEvent {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ReminderViewModel
    private lateinit var currentList: List<Reminder>
    private lateinit var adapter: ReminderAdapter
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
        binding.tvNoReminders.isVisible = false

        val reminderDatabase = ReminderDatabase.getDatabaseInstance(this)
        val reminderDao = reminderDatabase.reminderDao()
        val repository = ReminderRepository(reminderDao)
        viewModel = ViewModelProvider(this, ReminderViewModelFactory(repository))[ReminderViewModel::class.java]

        ////////////////////////////////////  recycler view



        adapter = ReminderAdapter(this)
        binding.apply {
            rvReminderList.layoutManager = LinearLayoutManager(this@MainActivity)
            rvReminderList.hasFixedSize()
            rvReminderList.adapter = adapter
        }
        viewModel.getReminders().observe(this, Observer {
            adapter.submitList(it)
            currentList = it
            binding.tvNoReminders.isVisible = it.isEmpty()
        })

        val swipeHandler = ItemTouchHelper(SwipeDelete(adapter))
        swipeHandler.attachToRecyclerView(binding.rvReminderList)

        binding.btnNewReminder.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog(){
        val dialogBinding = AddReminderDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var selectedDate = LocalDate.now()
        var selectedTime = LocalTime.now()

        var isDateFilled = false
        var isTimeFilled = false
        var isTitleFilled = false
        var isDescriptionFilled = false


        // DATE SETTING LOGIC //////////////////////////////////////////////////////////////////////

        val myCalendar = Calendar.getInstance()
        val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            isDateFilled = true
            selectedDate = LocalDate.of(year, month+1, dayOfMonth)
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.UK)
            dialogBinding.apply {
                tvDialogDate.text = sdf.format(myCalendar.time)
                tvDialogDate.setBackgroundResource(R.drawable.cancel_button_background)
            }
        }
        dialogBinding.ibDialogCalendar.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                onDateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.minDate = System.currentTimeMillis()
            dpd.show()
        }


        // TIME SETTING LOGIC //////////////////////////////////////////////////////////////////////

        val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            // to prevent past time selection
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDate.year)
                set(Calendar.MONTH, selectedDate.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, selectedDate.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if(cal.timeInMillis < System.currentTimeMillis()){
                Toast.makeText(this, "Please select a time in the future", Toast.LENGTH_LONG).show()
            }else{
                selectedTime = LocalTime.of(hourOfDay, minute)
                isTimeFilled = true
                val amOrPm = if(hourOfDay > 11) "pm" else "am"
                val hourToShow = if(hourOfDay > 12) hourOfDay - 12 else hourOfDay
                dialogBinding.apply {
                    tvDialogTime.text = String.format("%02d:%02d %s", hourToShow, minute, amOrPm)
                    tvDialogTime.setBackgroundResource(R.drawable.cancel_button_background)
                }
            }
        }

        dialogBinding.ibDialogClock.setOnClickListener {
            TimePickerDialog(this, onTimeSetListener, selectedTime.hour, selectedTime.minute, false).show()
        }

        // STORING THE DATA ////////////////////////////////////////////////////////////////////////
        dialogBinding.btnDialogSetReminder.setOnClickListener {
            isTitleFilled = dialogBinding.etDialogTitle.text.isNotBlank()
            isDescriptionFilled = dialogBinding.etDialogDesc.text.isNotBlank()
            when {
                !isTitleFilled -> showToast("Title cannot be empty!")
                !isDescriptionFilled -> showToast("Description cannot be empty!")
                !isDateFilled -> showToast("Please select a date first!")
                !isTimeFilled -> showToast("Please select a time first!")
                else -> {
                    val newReminder = Reminder(
                        id = 0,
                        title = dialogBinding.etDialogTitle.text.toString(),
                        description = dialogBinding.etDialogDesc.text.toString(),
                        date = selectedDate,
                        time = selectedTime
                    )
                    viewModel.addReminders(newReminder)
                    dialog.dismiss()
                }
            }
        }
        dialogBinding.btnDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Do you want to delete this reminder ?")
            .setPositiveButton("Yes"){ dialog, _ ->
                viewModel.deleteReminder(currentList[position].id)
                dialog.dismiss()
            }
            .setNegativeButton("No"){dialog, _ ->
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .show()
    }

}