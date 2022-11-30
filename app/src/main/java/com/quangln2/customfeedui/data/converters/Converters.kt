package com.quangln2.customfeedui.data.converters

import androidx.room.TypeConverter
import com.quangln2.customfeedui.data.models.datamodel.OfflineResource

class Converters {
    @TypeConverter
    fun fromResources(value: MutableList<OfflineResource>): String {
        return OfflineResource.mutableListOfflineResourceToJsonArray(value)
    }

    @TypeConverter
    fun stringToResources(value: String): MutableList<OfflineResource> {
        return OfflineResource.jsonArrayToMutableListOfflineResource(value)
    }
}