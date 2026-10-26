package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.StationType

/**
 * Real, honest emergency actions. Adventurer never claims to alert anyone on the rider's behalf —
 * these hand off to the phone dialer, the share sheet, or a maps app.
 */
object EmergencyActions {

    /** Opens the dialer with [number] pre-filled (no CALL_PHONE permission needed; one tap to call). */
    fun dial(context: Context, number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.filter { it.isDigit() || it == '+' }))
        launch(context, intent, "No phone app available")
    }

    /** Shares the rider's coordinates as a Google Maps link via WhatsApp, SMS, etc. */
    fun shareLocation(context: Context, latitude: Double?, longitude: Double?) {
        val text = if (latitude != null && longitude != null) {
            "I need help on my ride. My location: https://maps.google.com/?q=%.6f,%.6f".format(latitude, longitude)
        } else {
            "I need help on my ride. I couldn't get my GPS location — please call me."
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        launch(context, Intent.createChooser(send, "Send your location"), "No app available to share")
    }

    /** Searches the maps app for real places of this kind near the rider. */
    fun findNearby(context: Context, query: String) {
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query)))
        try {
            context.startActivity(geo)
        } catch (_: ActivityNotFoundException) {
            val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode("$query near me")))
            launch(context, web, "No maps app or browser available")
        }
    }

    fun searchQuery(type: StationType): String = when (type) {
        StationType.MECHANIC -> "motorcycle mechanic"
        StationType.PUNCTURE -> "tyre puncture repair"
        StationType.FUEL_STASH -> "petrol pump"
        StationType.HOSPITAL -> "hospital"
        StationType.DHABA -> "dhaba"
    }

    private fun launch(context: Context, intent: Intent, failure: String) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, failure, Toast.LENGTH_SHORT).show()
        }
    }
}
