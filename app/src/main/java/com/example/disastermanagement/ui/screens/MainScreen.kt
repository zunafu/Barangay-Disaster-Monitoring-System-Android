package com.example.disastermanagement.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.disastermanagement.R
import com.example.disastermanagement.data.database.Incident
import com.example.disastermanagement.data.database.User
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MainScreen(
    navController: NavHostController,
    incidents: List<Incident>,
    users: List<User>,
    isReportMode: Boolean,
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    mapCenter: GeoPoint?,
    onMapCenterChange: (GeoPoint?) -> Unit,
    locationPermissionGranted: Boolean,
    userId: String,
    userRole: String,
    onConfirmIncident: (Incident) -> Unit,
    onUpdateIncident: (Incident, String, String) -> Unit,
    onResolveIncident: (Incident) -> Unit,
    processingIncidentId: Int?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Remember MapView and persistent overlays
    val mapView = remember { MapView(context) }
    val myLocationOverlay = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) }
    
    val reportModeOverlay = remember {
        object : Overlay() {
            override fun onLongPress(e: MotionEvent, mapView: MapView): Boolean {
                val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                onLocationSelected(geoPoint)
                return true
            }
        }
    }

    // Lifecycle management to ensure map is active immediately
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Handle Location Tracking and Initial Centering efficiently
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            myLocationOverlay.enableMyLocation()
            // Try to center on my location once fix is obtained
            var attempts = 0
            while (attempts < 12) { 
                val location = myLocationOverlay.myLocation
                if (location != null) {
                    mapView.controller.setZoom(18.0)
                    mapView.controller.setCenter(location)
                    break
                }
                attempts++
                delay(500)
            }
        } else {
            myLocationOverlay.disableMyLocation()
        }
    }

    // Handle map centering from external requests (e.g., clicking an incident in logs)
    LaunchedEffect(mapCenter) {
        mapCenter?.let { 
            mapView.controller.animateTo(it, 18.0, 1000L)
            onMapCenterChange(null) // Consume the event
        }
    }

    val disasterTypes = mapOf(
        "Fire" to "🔥",
        "Flood" to "🌊",
        "Earthquake" to "🏚️",
        "Typhoon" to "🌀",
        "Landslide" to "⛰️",
        "Accident" to "🚗",
        "Other" to "❓"
    )

    fun createMarkerIcon(context: Context, emoji: String, severity: String, status: String, isSelected: Boolean = false): BitmapDrawable {
        val mainRadius = 50f
        val badgeRadius = 20f
        val size = (mainRadius * 2 + badgeRadius * 2).toInt()

        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val mainCx = mainRadius + badgeRadius
        val mainCy = mainRadius + badgeRadius

        // Main circle
        paint.color = if (isSelected) Color.YELLOW else Color.WHITE
        canvas.drawCircle(mainCx, mainCy, mainRadius, paint)
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(mainCx, mainCy, mainRadius - 2f, paint)

        // Main emoji
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 60f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(emoji, mainCx, mainCy - ((paint.descent() + paint.ascent()) / 2), paint)

        // Status badge (upper left)
        val statusBadgeCx = badgeRadius
        val statusBadgeCy = badgeRadius

        paint.style = Paint.Style.FILL
        paint.color = when (status) {
            "Responding" -> Color.parseColor("#FFA500")
            "In Area" -> Color.BLUE
            "Reported" -> Color.LTGRAY
            else -> Color.TRANSPARENT
        }
        canvas.drawCircle(statusBadgeCx, statusBadgeCy, badgeRadius, paint)
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(statusBadgeCx, statusBadgeCy, badgeRadius - 1.5f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 25f
        paint.textAlign = Paint.Align.CENTER
        val statusEmoji = when (status) {
            "Responding" -> "🚨"
            "In Area" -> "📍"
            "Reported" -> "📝"
            else -> " "
        }
        canvas.drawText(statusEmoji, statusBadgeCx, statusBadgeCy - ((paint.descent() + paint.ascent()) / 2), paint)

        // Severity badge (upper right)
        if (severity != "None") {
            val severityBadgeCx = size - badgeRadius
            val severityBadgeCy = badgeRadius

            paint.style = Paint.Style.FILL
            paint.color = when (severity) {
                "Low" -> Color.GREEN
                "Medium" -> Color.YELLOW
                "High" -> Color.rgb(255, 165, 0) // Orange
                "Critical" -> Color.RED
                else -> Color.LTGRAY
            }
            canvas.drawCircle(severityBadgeCx, severityBadgeCy, badgeRadius, paint)
            paint.color = Color.DKGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawCircle(severityBadgeCx, severityBadgeCy, badgeRadius - 1.5f, paint)
        }

        return BitmapDrawable(context.resources, bitmap)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(false)
                    controller.setZoom(9.0)
                    controller.setCenter(GeoPoint(14.5995, 120.9842))
                }
            },
            update = { map ->
                // Maintain persistent overlays
                if (!map.overlays.contains(myLocationOverlay)) map.overlays.add(myLocationOverlay)
                
                if (isReportMode) {
                    if (!map.overlays.contains(reportModeOverlay)) map.overlays.add(reportModeOverlay)
                } else {
                    map.overlays.remove(reportModeOverlay)
                }

                // Clear previous markers to prevent performance issues and duplicates
                val markersToRemove = map.overlays.filterIsInstance<Marker>()
                map.overlays.removeAll(markersToRemove)

                // Add current selection marker if in report mode
                selectedLocation?.let { location ->
                    val marker = Marker(map)
                    marker.position = location
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.icon = createMarkerIcon(context, "📍", "None", "Reported", true)
                    map.overlays.add(marker)
                }

                // Add all incident markers from the state
                incidents.forEach { incident ->
                    val marker = Marker(map)
                    marker.position = incident.location
                    marker.setAnchor(0.5f, 0.5f)
                    marker.title = incident.title
                    marker.icon = createMarkerIcon(context, disasterTypes[incident.type] ?: "❓", incident.severity, incident.status)
                    marker.setOnMarkerClickListener { m, _ ->
                        InfoWindow.closeAllInfoWindowsOn(map)

                        val infoWindow = object: InfoWindow(R.layout.custom_bubble, map) {
                            override fun onOpen(item: Any?) {
                                val defaultMarker = item as Marker
                                val title = defaultMarker.title

                                mView.findViewById<TextView>(R.id.bubble_title).text = title
                                mView.findViewById<TextView>(R.id.bubble_description).text = incident.type
                                
                                val severityView = mView.findViewById<TextView>(R.id.bubble_severity)
                                if (incident.severity != "None") {
                                    severityView.text = incident.severity
                                    severityView.background.colorFilter = PorterDuffColorFilter(when (incident.severity) {
                                        "Low" -> Color.GREEN
                                        "Medium" -> Color.YELLOW
                                        "High" -> Color.rgb(255, 165, 0) // Orange
                                        "Critical" -> Color.RED
                                        else -> Color.GRAY
                                    }, PorterDuff.Mode.SRC_IN)
                                    severityView.visibility = View.VISIBLE
                                } else {
                                    severityView.visibility = View.GONE
                                }

                                val statusView = mView.findViewById<TextView>(R.id.bubble_status)
                                statusView.text = incident.status
                                statusView.background.colorFilter = PorterDuffColorFilter(when (incident.status) {
                                    "Responding" -> Color.parseColor("#FFA500")
                                    "In Area" -> Color.BLUE
                                    "Reported" -> Color.LTGRAY
                                    else -> Color.TRANSPARENT
                                }, PorterDuff.Mode.SRC_IN)

                                mView.findViewById<android.widget.ImageView>(R.id.bubble_close).setOnClickListener {
                                    close()
                                }
                                val moreInfo = mView.findViewById<android.widget.Button>(R.id.bubble_moreinfo)
                                moreInfo.setOnClickListener { 
                                    navController.navigate("incident_detail/${incident.id}")
                                    close()
                                }
                            }
                            override fun onClose() {}
                        }
                        marker.infoWindow = infoWindow
                        marker.showInfoWindow()
                        true
                    }
                    map.overlays.add(marker)
                }
                map.invalidate()
            }
        )
        
        // Floating Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            FloatingActionButton(onClick = {
                myLocationOverlay.myLocation?.let { myLocation ->
                    mapView.controller.animateTo(myLocation, 18.0, 500L)
                }
            }) {
                Icon(Icons.Filled.MyLocation, contentDescription = "My Location")
            }
        }
    }
}
