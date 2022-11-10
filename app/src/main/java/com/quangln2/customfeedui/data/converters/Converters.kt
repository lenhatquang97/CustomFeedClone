package com.quangln2.customfeedui.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quangln2.customfeedui.data.models.datamodel.OfflineResource

class Converters {
    @TypeConverter
    fun fromResources(value: MutableList<OfflineResource>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun stringToResources(value: String): MutableList<OfflineResource> {
        val type = object : TypeToken<MutableList<OfflineResource>>() {}.type
        return Gson().fromJson(value, type)
    }
}