package com.shiv.reminder

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromDateToString(date: LocalDate):String{
        return date.toString()
    }

    @TypeConverter
    fun formStringToDate(string: String):LocalDate{
        return LocalDate.parse(string)
    }

    @TypeConverter
    fun fromTimeToString(time: LocalTime): String{
        return time.toString()
    }

    @TypeConverter
    fun fromStringToTime(string: String): LocalTime{
        return LocalTime.parse(string)
    }

}