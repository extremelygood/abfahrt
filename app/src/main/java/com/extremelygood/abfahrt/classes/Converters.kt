package com.extremelygood.abfahrt.classes

import android.location.Location
import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { encodeDefaults = true }

    @TypeConverter
    fun fromGeoLocation(value: GeoLocation?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toGeoLocation(value: String?): GeoLocation? {
        return value?.let { json.decodeFromString<GeoLocation>(it) }
    }

    @TypeConverter
    fun fromLocation(location: Location?): String? {
        return location?.let { "${it.latitude},${it.longitude}" }
    }

    @TypeConverter
    fun toLocation(data: String?): Location? {
        return data?.split(",")?.let {
            if (it.size != 2) return null
            Location("room").apply {
                latitude = it[0].toDoubleOrNull() ?: 0.0
                longitude = it[1].toDoubleOrNull() ?: 0.0
            }
        }
    }
}
