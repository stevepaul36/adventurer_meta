package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalReviewScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    var checkAudience by remember { mutableStateOf(false) }
    var checkRouteMasking by remember { mutableStateOf(false) }
    var checkMediaRights by remember { mutableStateOf(false) }
    var isConfirmed by remember { mutableStateOf(false) }

    val allConfirmed = checkAudience && checkRouteMasking && checkMediaRights

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "FINAL REVIEW",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SafeMint.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "06 CONSENT GATE",
                                    color = SafeMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Review is a real step, not a decorative dialog",
                            style = MaterialTheme.typography.labelSmall,
                            color = FieldMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("final_review_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Story Studio", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatePanel)
            )
        },
        containerColor = FieldNavy,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Ready when you are
            item {
                Column {
                    Text(
                        "READY WHEN YOU ARE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TrailOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "One last look\nbefore it leaves.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "The preview reflects edits from Story Studio. Audience, precise-route treatment, and delayed location are summarized before saving.",
                        color = FieldMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Live Story Preview Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("final_review_preview_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(TrailOrange.copy(alpha = 0.7f), SafeMint.copy(alpha = 0.5f)))
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
                                contentDescription = "Story preview cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TrailOrange
                                ) {
                                    Text(
                                        "ROUTE JOURNAL",
                                        color = FieldNavy,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "The Long Way Home: Varandha Ridge",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Caption Preview
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "CAPTION PREVIEW",
                                color = FieldMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Took the scenic route through monsoon mist. Came back with a full camera roll and a clearer head. 🏍️🌧️",
                                color = Color(0xFFDCE3DF),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Sharing Summary Section with Safe Mint status indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderNavy)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SHARING SUMMARY",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafeMint.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "REVIEW REQUIRED",
                                    color = SafeMint,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SummaryRow(
                            title = "Audience",
                            subtitle = "Only me · private local draft",
                            icon = Icons.Default.Lock
                        )
                        HorizontalDivider(color = BorderNavy, modifier = Modifier.padding(vertical = 10.dp))
                        SummaryRow(
                            title = "Route detail",
                            subtitle = "Start/end masked · 24h delayed context",
                            icon = Icons.Default.VisibilityOff
                        )
                        HorizontalDivider(color = BorderNavy, modifier = Modifier.padding(vertical = 10.dp))
                        SummaryRow(
                            title = "Media and location permission",
                            subtitle = "Includes selected verified moments from this ride",
                            icon = Icons.Default.VerifiedUser
                        )
                    }
                }
            }

            // The 3 Mandatory Consent Checkboxes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (allConfirmed) SafeMint else BorderNavy)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "EXPLICIT CONSENT CHECKS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TrailOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ConsentCheckbox(
                            checked = checkAudience,
                            onCheckedChange = { checkAudience = it },
                            text = "I reviewed the audience and am comfortable with who can see this."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ConsentCheckbox(
                            checked = checkRouteMasking,
                            onCheckedChange = { checkRouteMasking = it },
                            text = "I checked the map, route masking and location timing."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ConsentCheckbox(
                            checked = checkMediaRights,
                            onCheckedChange = { checkMediaRights = it },
                            text = "I own or have permission to share these photos, clips and voices."
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isConfirmed = true
                            Toast.makeText(context, "Demo story confirmed locally — nothing was published.", Toast.LENGTH_SHORT).show()
                        },
                        enabled = allConfirmed && !isConfirmed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TrailOrange,
                            disabledContainerColor = BorderNavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_save_demo_story_button")
                    ) {
                        if (isConfirmed) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = FieldNavy)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✓ Demo review complete", color = FieldNavy, fontWeight = FontWeight.ExtraBold)
                        } else {
                            Text(
                                "Confirm & save demo story",
                                color = if (allConfirmed) FieldNavy else FieldMuted,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateBack() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("← Back to edit story", fontWeight = FontWeight.Bold)
                    }

                    if (isConfirmed) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.FEED) },
                            colors = ButtonDefaults.buttonColors(containerColor = SafeMint),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Return to Basecamp", color = FieldNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Prototype Boundary Disclosure
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SlatePanelAlt,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Prototype Boundary: This concept saves nothing to a remote backend and publishes nothing publicly. Production publishing requires authenticated server enforcement, storage disclosure and privacy checks.",
                        color = FieldMuted,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = TrailOrange, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(subtitle, color = FieldMuted, fontSize = 11.sp)
            }
        }
        Surface(
            shape = CircleShape,
            color = SafeMint,
            modifier = Modifier.size(8.dp)
        ) {}
    }
}

@Composable
private fun ConsentCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SafeMint,
                checkmarkColor = FieldNavy,
                uncheckedColor = BorderNavy
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (checked) Color.White else FieldMuted,
            fontSize = 12.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            lineHeight = 16.sp
        )
    }
}
