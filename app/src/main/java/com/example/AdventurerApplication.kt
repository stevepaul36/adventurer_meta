package com.example

import android.app.Application
import com.example.data.billing.AdventurerProBilling

/**
 * One-time SDK initialization for app-wide services.
 * No private credentials are read or stored here.
 */
class AdventurerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdventurerProBilling.initialize(this)
    }
}
