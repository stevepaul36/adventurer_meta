package com.example

import android.app.Application
import android.util.Log
import com.example.data.billing.AdventurerProBilling
import com.google.firebase.FirebaseApp
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
            Log.w(TAG, "Firebase is not configured. Online AI and billing are disabled until setup is completed.")
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

        // Also listen for a successful sign-in after a transient network failure.
        auth.addAuthStateListener { firebaseAuth ->
            firebaseAuth.currentUser?.uid?.let { uid ->
                AdventurerProBilling.initialize(this, uid)
            }
        }

        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnFailureListener { error ->
                Log.w(TAG, "Firebase anonymous sign-in failed; billing will retry after authentication succeeds.", error)
            }
        } else {
            AdventurerProBilling.initialize(this, auth.currentUser!!.uid)
        }
    }

    private companion object {
        const val TAG = "AdventurerApplication"
    }
}
