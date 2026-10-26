package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.TripBudgetEstimate
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (caption: String, imageUrl: String?) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Adventure Post", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("What happened on the trail?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_caption_input"),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdventureOrange,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Photo URL (Optional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_image_url_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AdventureOrange,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        onSubmit(caption, if (imageUrl.isBlank()) null else imageUrl)
                    }
                },
                enabled = caption.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                modifier = Modifier.testTag("submit_post_button")
            ) {
                Text("Publish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CommentsBottomSheet(
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var newCommentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Trail Comments (${comments.size})", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
            ) {
                if (comments.isEmpty()) {
                    Text(
                        text = "No comments yet. Start the conversation!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = comment.user.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = comment.user.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold
                                    )
                                    Text(
                                        text = comment.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Write a comment...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AdventureOrange,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.testTag("send_comment_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = AdventureOrange)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TripBudgetModal(
    distanceKm: Int,
    onDismiss: () -> Unit
) {
    var days by remember { mutableStateOf(8) }
    var mileage by remember { mutableStateOf(28.0) }
    var stayPerNight by remember { mutableStateOf(1800.0) }
    val estimate = remember(distanceKm, days, mileage, stayPerNight) {
        TripBudgetEstimate(
            distanceKm = distanceKm,
            bikeMileageKmpl = mileage,
            stayPerNightInr = stayPerNight,
            daysCount = days
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = AmberGold)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Expedition Budget Calculator", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Total Expedition Route: $distanceKm km",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HighAltitudeCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration (Days): $days", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Row {
                        IconButton(onClick = { if (days > 2) days-- }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                        }
                        IconButton(onClick = { if (days < 30) days++ }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BudgetLineItem("Fuel (${estimate.totalFuelLiters.toInt()} L @ ₹104/L)", "₹${estimate.totalFuelCostInr.toInt()}")
                        BudgetLineItem("Stay ($days nights @ ₹${stayPerNight.toInt()})", "₹${estimate.totalStayCostInr.toInt()}")
                        BudgetLineItem("Food & Hydration ($days days)", "₹${estimate.totalFoodCostInr.toInt()}")
                        BudgetLineItem("Inner Line & Wildlife Permits", "₹${estimate.permitCostInr.toInt()}")
                        BudgetLineItem("Emergency Spares & Medical Buffer", "₹${estimate.sparePartsBufferInr.toInt()}")
                        Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Total", fontWeight = FontWeight.Bold, color = AmberGold)
                            Text("₹${estimate.totalEstimateInr.toInt()}", fontWeight = FontWeight.Bold, color = AmberGold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SafetySosDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EMERGENCY SOS TRANSMITTER", fontWeight = FontWeight.Bold, color = DangerRed)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DangerRed.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DangerRed)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "BROADCASTING DISTRESS COORDINATES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 Lat: 32.5512° N, Long: 77.1724° E\nAltitude: 14,280 ft (Near Sarchu Plains)\nBattery: 82% | GPS Fix: High Accuracy",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "High-Pass Emergency Contacts:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• BRO / Police Control: 112\n• ITBP Mountain Rescue: 1982-252111\n• Jispa Mechanic SOS: +91 94180 12345",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCBD5E1)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
            ) {
                Text("Stop SOS Broadcast", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun BudgetLineItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
