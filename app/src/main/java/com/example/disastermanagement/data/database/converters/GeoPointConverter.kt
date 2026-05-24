package com.example.disastermanagement.data.database.converters

import androidx.room.TypeConverter
import org.osmdroid.util.GeoPoint

class GeoPointConverter {
    @TypeConverter
    fun fromGeoPoint(geoPoint: GeoPoint): String {
        return "${geoPoint.latitude},${geoPoint.longitude}"
    }

    @TypeConverter
    fun toGeoPoint(value: String): GeoPoint {
        val parts = value.split(",")
        return GeoPoint(parts[0].toDouble(), parts[1].toDouble())
    }
}