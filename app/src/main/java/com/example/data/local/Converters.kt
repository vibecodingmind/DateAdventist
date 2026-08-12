package com.example.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListString(value: List<String>?): String {
        return value?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toListString(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;;")
    }
}
