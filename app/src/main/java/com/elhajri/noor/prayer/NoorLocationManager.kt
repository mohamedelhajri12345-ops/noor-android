package com.elhajri.noor.prayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.elhajri.noor.data.City
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.Prefs
import com.elhajri.noor.notification.AdhanScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

object NoorLocationManager {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    /** Finds closest embedded city in DataLoader.cities using Haversine distance formula */
    fun findClosestCity(context: Context, lat: Double, lng: Double): City {
        val cities = DataLoader.cities(context)
        if (cities.isEmpty()) return City("مكة المكرمة", 21.4225, 39.8262, "السعودية", "Asia/Riyadh")

        var closest = cities[0]
        var minDistance = Double.MAX_VALUE

        for (c in cities) {
            val dist = haversineKm(lat, lng, c.lat, c.lng)
            if (dist < minDistance) {
                minDistance = dist
                closest = c
            }
        }
        return closest
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /** Retrieves latest device location via GPS or Network provider */
    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
        if (!hasLocationPermission(context)) {
            callback(null)
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            callback(null)
            return
        }

        try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            var bestLoc: Location? = null

            for (p in providers) {
                if (lm.isProviderEnabled(p)) {
                    val loc = lm.getLastKnownLocation(p)
                    if (loc != null) {
                        if (bestLoc == null || loc.accuracy < bestLoc.accuracy || loc.time > bestLoc.time) {
                            bestLoc = loc
                        }
                    }
                }
            }

            if (bestLoc != null) {
                callback(bestLoc)
                return
            }

            val provider = if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationManager.NETWORK_PROVIDER
            } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else null

            if (provider != null) {
                var handled = false
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (!handled) {
                            handled = true
                            try { lm.removeUpdates(this) } catch (_: Exception) {}
                            callback(location)
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            } else {
                callback(null)
            }
        } catch (_: Exception) {
            callback(null)
        }
    }

    /**
     * Executes automatic location detection: reads coordinates, maps to nearest world city,
     * updates Prefs and re-schedules prayer times & adhan notifications.
     */
    fun updateAutoLocation(
        context: Context,
        force: Boolean = false,
        onUpdated: ((City?) -> Unit)? = null
    ) {
        if (!force && !Prefs.isAutoLocation(context)) {
            onUpdated?.invoke(null)
            return
        }

        getCurrentLocation(context) { loc ->
            if (loc == null) {
                onUpdated?.invoke(null)
                return@getCurrentLocation
            }

            CoroutineScope(Dispatchers.IO).launch {
                val closestCity = findClosestCity(context, loc.latitude, loc.longitude)

                var finalCity = closestCity
                try {
                    if (Geocoder.isPresent()) {
                        val geocoder = Geocoder(context, Locale("ar"))
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: closestCity.name
                            val countryName = addr.countryName ?: closestCity.country
                            finalCity = City(
                                name = cityName,
                                lat = loc.latitude,
                                lng = loc.longitude,
                                country = countryName,
                                timezone = closestCity.timezone
                            )
                        }
                    }
                } catch (_: Exception) {}

                // Save updated city to Prefs
                Prefs.setCity(context, finalCity)

                // Refresh prayer times cache & adhan schedule
                try {
                    PrayerRepository.getTimings(
                        context,
                        lat = finalCity.lat,
                        lng = finalCity.lng,
                        country = finalCity.country
                    )
                    AdhanScheduler.scheduleNextAdhan(context)
                } catch (_: Exception) {}

                withContext(Dispatchers.Main) {
                    onUpdated?.invoke(finalCity)
                }
            }
        }
    }
}
