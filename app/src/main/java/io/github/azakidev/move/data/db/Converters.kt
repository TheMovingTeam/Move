package io.github.azakidev.move.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.azakidev.move.data.Capabilities
import io.github.azakidev.move.data.TimeFormat

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCapabilitiesList(capabilities: List<Capabilities>?): String? {
        return capabilities?.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toCapabilitiesList(capabilitiesString: String?): List<Capabilities>? {
        return capabilitiesString?.split(",")?.mapNotNull {
            try {
                Capabilities.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun fromTimeFormat(timeFormat: TimeFormat?): String? {
        return gson.toJson(timeFormat)
    }

    @TypeConverter
    fun toTimeFormat(timeFormatString: String?): TimeFormat? {
        return gson.fromJson(timeFormatString, object : com.google.gson.reflect.TypeToken<TimeFormat>() {}.type)
    }

    @TypeConverter
    fun fromIntegerList(list: List<Int>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toIntegerList(json: String?): List<Int>? {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type)
    }
}