package com.example.disastermanagement.ui.screens.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.IOException
import kotlin.coroutines.resume

suspend fun getAddressFromLocation(context: Context, location: GeoPoint): String {
    return withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                if (addresses.isNotEmpty()) {
                                    continuation.resume(addresses[0].getAddressLine(0) ?: "Unknown Location")
                                } else {
                                    continuation.resume("Unknown Location")
                                }
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume("Error getting address")
                            }
                        }
                    }
                )
            }
        } else {
            @Suppress("DEPRECATION")
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses?.isNotEmpty() == true) {
                    addresses[0].getAddressLine(0) ?: "Unknown Location"
                } else {
                    "Unknown Location"
                }
            } catch (e: IOException) {
                "Error getting address"
            }
        }
    }
}
