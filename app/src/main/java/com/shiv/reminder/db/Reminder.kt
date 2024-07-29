package com.shiv.reminder.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "reminders_table")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val description: String,
    val date: LocalDate,
    val time: LocalTime
)
