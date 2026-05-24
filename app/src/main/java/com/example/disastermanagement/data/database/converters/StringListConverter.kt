package com.example.disastermanagement.data.database.converters

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(";,;")
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return value.split(";,;")
    }
}