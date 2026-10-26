package com.example

import android.app.Application
import com.example.data.billing.AdventurerProBilling
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth

/**
 * Installs App Check before any authenticated Functions/Storage request, establishes
 * a Firebase UID, and ties that identity to RevenueCat for server-side entitlement checks.
 */
class AdventurerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (FirebaseApp.getApps(this).isEmpty()) {
            AdventurerProBilling.initialize(this, null)
            return
        }

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            AdventurerProBilling.initialize(this, currentUser.uid)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    AdventurerProBilling.initialize(this, result.user?.uid)
                }
                .addOnFailureListener {
                    AdventurerProBilling.initialize(this, null)
                }
        }
    }
}
