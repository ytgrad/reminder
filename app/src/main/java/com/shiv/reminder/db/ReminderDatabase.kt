package com.shiv.reminder.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Reminder::class], version = 1)
@TypeConverters(Converters::class)
abstract class ReminderDatabase: RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    
    companion object{
        private var INSTANCE: ReminderDatabase? = null
        fun getDatabaseInstance(context: Context): ReminderDatabase{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(context.applicationContext, ReminderDatabase::class.java, "reminder-db")
                    .build()
            }
            return INSTANCE!!
        }
    }
}