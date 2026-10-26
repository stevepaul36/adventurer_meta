package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.billing.AdventurerProBilling
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.viewmodel.AdventurerViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallScreen(viewModel: AdventurerViewModel, modifier: Modifier = Modifier) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val activity = context.findActivity()
    val billing by AdventurerProBilling.state.collectAsState()

    LaunchedEffect(Unit) {
        AdventurerProBilling.refresh()
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFF0B0F17),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Adventurer Pro", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("More story. More freedom.", color = AmberGold, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151C2B)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(AdventureOrange, AmberGold))
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberGold)
                        Text(
                            if (billing.isPro) "You're riding with Pro"
                            else "Your adventures deserve a story",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Turn your footage into a story: discover candidate highlights, generate multilingual captions, and keep control of every clip before export.",
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                FeatureRow("AI highlight discovery", "Find candidate moments in your actual ride footage.")
                FeatureRow("AI story captions", "Generate grounded social captions in supported languages.")
                FeatureRow("30 AI generations per month", "Usage resets each UTC month. Successful highlight analysis and caption generation both count.")
                FeatureRow("Local MP4 export", "Render selected clips on-device; review AI timestamps before sharing.")
            }

            if (billing.isPro) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF12352D))
                    ) {
                        Text(
                            "Adventurer Pro is active on this store account.",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (!billing.isConfigured) {
                item {
                    StatusCard(
                        message = billing.errorMessage
                            ?: "Billing isn't configured in this build. Add a RevenueCat test SDK key for debug and a Google Play public SDK key for release."
                    )
                }
            }

            if (billing.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = AdventureOrange)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            if (billing.packages.isNotEmpty()) {
                items(billing.packages, key = { it.identifier }) { purchasePackage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(purchasePackage.product.title, color = Color.White, fontWeight = FontWeight.Bold)
                            if (purchasePackage.product.description.isNotBlank()) {
                                Text(purchasePackage.product.description, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                purchasePackage.product.price.formatted,
                                color = AmberGold,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    if (activity == null) {
                                        // The screen must be hosted by an Activity for Play Billing.
                                    } else {
                                        AdventurerProBilling.purchase(activity, purchasePackage)
                                    }
                                },
                                enabled = billing.isConfigured && !billing.isLoading && !billing.isPro && activity != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                            ) {
                                Text(if (billing.isPro) "Subscription active" else "Continue with Google Play", color = Color.Black)
                            }
                        }
                    }
                }
            }

            if (billing.errorMessage != null && billing.isConfigured) {
                item { StatusCard(message = billing.errorMessage!!) }
            }
            if (billing.statusMessage != null) {
                item { StatusCard(message = billing.statusMessage!!) }
            }

            item {
                OutlinedButton(
                    onClick = { AdventurerProBilling.restorePurchases() },
                    enabled = billing.isConfigured && !billing.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(Modifier.height(4.dp))
                    Text("Restore purchases")
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF94A3B8))
                    Text(
                        "Subscriptions are processed by Google Play. Price, trial length and renewal terms are shown by Google Play before purchase. Manage or cancel in Google Play subscriptions. See the app's privacy policy and terms before launch.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberGold)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color(0xFFCBD5E1), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatusCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF30202A))
    ) {
        Text(message, color = Color(0xFFFECACA), modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall)
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
