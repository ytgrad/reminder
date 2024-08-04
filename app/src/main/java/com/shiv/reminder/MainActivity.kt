package com.shiv.reminder

import android.annotation.SuppressLint
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
import com.shiv.reminder.databinding.EditReminderDialogBinding
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

        val swipeHandler = ItemTouchHelper(SwipeToDelete(adapter))
        swipeHandler.attachToRecyclerView(binding.rvReminderList)

        binding.btnNewReminder.setOnClickListener {
            showDialog()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDialog(){
        val dialogBinding = AddReminderDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var selectedDate = LocalDate.now()
        var selectedTime = LocalTime.now()

        var isDateFilled = false
        var isTimeFilled = false
        var isTitleFilled : Boolean
        var isDescriptionFilled : Boolean


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

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onItemLongClick(position: Int) {
        val currReminder = currentList[position]
        val editBinding = EditReminderDialogBinding.inflate(layoutInflater)
        val editDialog = Dialog(this)
        editDialog.setContentView(editBinding.root)
        editDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var newDate = currReminder.date
        var newTime = currReminder.time
        val amOrPm = if (currReminder.time.hour > 11) "pm" else "am"
        val hourToShow = if (currReminder.time.hour > 12) currReminder.time.hour - 12 else currReminder.time.hour
        editBinding.apply {
            etDialogTitle.setText(currReminder.title)
            etDialogDesc.setText(currReminder.description)
            tvDialogTime.text = String.format("%02d:%02d %s", hourToShow, currReminder.time.minute, amOrPm)
            tvDialogDate.text = "${currReminder.date.dayOfMonth} ${currReminder.date.month.toString()
                .slice(0..2).lowercase().replaceFirstChar { a -> a.uppercase() }} ${currReminder.date.year}"
            btnDialogCancel.setOnClickListener { editDialog.dismiss() }

            // EDIT DATE
            ibDialogCalendar.setOnClickListener{
                val myCalendar = Calendar.getInstance()
                val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    myCalendar.set(Calendar.YEAR, year)
                    myCalendar.set(Calendar.MONTH, month)
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    newDate = LocalDate.of(year, month+1, dayOfMonth)
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.UK)
                    editBinding.apply {
                        tvDialogDate.text = sdf.format(myCalendar.time)
                        tvDialogDate.setBackgroundResource(R.drawable.cancel_button_background)
                    }
                }
                val dpd = DatePickerDialog(
                    this@MainActivity,
                    onDateSetListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)
                )
                dpd.datePicker.minDate = System.currentTimeMillis()
                dpd.show()
            }

            // EDIT TIME
            val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                // to prevent past time selection
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, newDate.year)
                    set(Calendar.MONTH, newDate.monthValue - 1)
                    set(Calendar.DAY_OF_MONTH, newDate.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if(cal.timeInMillis < System.currentTimeMillis()){
                    Toast.makeText(this@MainActivity, "Please select a time in the future", Toast.LENGTH_LONG).show()
                }else{
                    newTime = LocalTime.of(hourOfDay, minute)
                    val amPm = if(hourOfDay > 11) "pm" else "am"
                    val hour2Show = if(hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    editBinding.apply {
                        tvDialogTime.text = String.format("%02d:%02d %s", hour2Show, minute, amPm)
                        tvDialogTime.setBackgroundResource(R.drawable.cancel_button_background)
                    }
                }
            }
            ibDialogClock.setOnClickListener {
                TimePickerDialog(this@MainActivity, onTimeSetListener, newTime.hour, newTime.minute, false).show()
            }

            // PUSH CHANGES
            btnDialogSetReminder.setOnClickListener {
                viewModel.updateReminder(
                    Reminder(
                        id = currReminder.id,
                        title = etDialogTitle.text.toString(),
                        description = etDialogDesc.text.toString(),
                        date = newDate,
                        time = newTime
                    )
                )
                Toast.makeText(this@MainActivity, "title: ${etDialogTitle.text}", Toast.LENGTH_SHORT).show()
                editDialog.dismiss()
            }
        }
        editDialog.show()
    }

}