package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AdventurerViewModel
import com.example.viewmodel.Screen

data class RouteChapter(
    val indexStr: String,
    val title: String,
    val detail: String,
    val altitude: String,
    val isVerified: Boolean = false
)

data class PrepCheckItem(
    val id: String,
    val label: String,
    var isDone: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteBriefScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    var isSavedToTripBoard by remember { mutableStateOf(true) }
    var prepItems by remember {
        mutableStateOf(
            listOf(
                PrepCheckItem("contact", "Trusted contact chosen (Alex S.)", true),
                PrepCheckItem("return_time", "Expected return time set (6:30 PM)", true),
                PrepCheckItem("fuel", "Fuel range and reserve buffer calculated", true),
                PrepCheckItem("gear", "Rain gear and windproof thermal layer packed", true),
                PrepCheckItem("offline_map", "Offline route candidate cached to device", true),
                PrepCheckItem("first_aid", "First aid and puncture repair kit inspected", true),
                PrepCheckItem("permit", "High pass seasonal permit check", false),
                PrepCheckItem("radio", "Group radio channel frequency sync (146.520 MHz)", false)
            )
        )
    }

    val completedCount = prepItems.count { it.isDone }
    val totalCount = prepItems.size

    val chapters = remember {
        listOf(
            RouteChapter("01", "Start · North Overlook", "Check fuel and road status before descent", "1,420 m", true),
            RouteChapter("02", "Creek Crossing", "Water break · access unverified in monsoon runoff", "1,180 m", false),
            RouteChapter("03", "Ridge Viewpoint", "Photo moment · ridge wind speed check", "1,650 m", true),
            RouteChapter("04", "Finish · Valley Shelter", "Expected arrival 6:30 PM · check-in ping", "980 m", true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "ROUTE BRIEF",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TrailOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "03 CANDIDATE",
                                    color = TrailOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Turn a candidate into a safe plan",
                            style = MaterialTheme.typography.labelSmall,
                            color = FieldMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("route_brief_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Field Atlas", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isSavedToTripBoard = !isSavedToTripBoard },
                        modifier = Modifier.testTag("route_brief_save_action")
                    ) {
                        Icon(
                            imageVector = if (isSavedToTripBoard) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSavedToTripBoard) "Saved" else "Save route",
                            tint = if (isSavedToTripBoard) SafeMint else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatePanel)
            )
        },
        bottomBar = {
            Surface(
                color = SlatePanel,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { isSavedToTripBoard = !isSavedToTripBoard },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isSavedToTripBoard) SafeMint else Color.White
                        ),
                        modifier = Modifier
                            .weight(0.45f)
                            .height(50.dp)
                            .testTag("save_to_trip_board_button")
                    ) {
                        Icon(
                            if (isSavedToTripBoard) Icons.Default.Check else Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSavedToTripBoard) "Saved" else "Save trip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.LIVE_TRACKER) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TrailOrange),
                        modifier = Modifier
                            .weight(0.55f)
                            .height(50.dp)
                            .testTag("use_plan_enter_field_button")
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = FieldNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use plan → Field", color = FieldNavy, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
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
            // Provenance Notice
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AmberGold.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "MAP CANDIDATES · NOT VERIFIED ROUTING",
                                color = AmberGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                            Text(
                                "All route lines and duration estimates are informational candidate models. Inspect conditions before leaving.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Route Candidate Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlatePanel),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(TrailOrange.copy(alpha = 0.8f), SafeMint.copy(alpha = 0.4f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ridge & River Loop",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SafeMint.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    "INTERMEDIATE",
                                    color = SafeMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Mixed pavement and loose gravel canyon loop with river water crossings and mountain ridge overlooks.",
                            color = FieldMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Key Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RouteMetricBox("ESTIMATE", "74 km", TrailOrange)
                            RouteMetricBox("STOPS", "4 Planned", AmberGold)
                            RouteMetricBox("TIME RANGE", "3h 15m", HighAltitudeCyan)
                        }
                    }
                }
            }

            // Route Chapters Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ROUTE CHAPTERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TrailOrange
                    )
                    Text(
                        "${chapters.size} Stops Configured",
                        style = MaterialTheme.typography.labelSmall,
                        color = FieldMuted
                    )
                }
            }

            items(chapters.size) { index ->
                val chapter = chapters[index]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SlatePanelAlt,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (chapter.isVerified) SafeMint.copy(alpha = 0.15f) else TrailOrange.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = chapter.indexStr,
                                    fontWeight = FontWeight.Black,
                                    color = if (chapter.isVerified) SafeMint else TrailOrange,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chapter.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (!chapter.isVerified) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AmberGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "UNVERIFIED",
                                            color = AmberGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = chapter.detail,
                                color = FieldMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = chapter.altitude,
                            color = HighAltitudeCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Prep Status Checklist (6/8 COMPLETE)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Checklist, contentDescription = null, tint = SafeMint, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "PREPARATION CHECKLIST",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SafeMint.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$completedCount/$totalCount COMPLETE",
                                    color = SafeMint,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Safety requirements and departure checks before entering Field Mode.",
                            color = FieldMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        prepItems.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        prepItems = prepItems.toMutableList().also {
                                            it[idx] = item.copy(isDone = !item.isDone)
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { checked ->
                                        prepItems = prepItems.toMutableList().also {
                                            it[idx] = item.copy(isDone = checked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SafeMint,
                                        checkmarkColor = FieldNavy,
                                        uncheckedColor = BorderNavy
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.label,
                                    color = if (item.isDone) Color.White else FieldMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (item.isDone) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteMetricBox(label: String, value: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SlatePanelAlt,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderNavy),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = FieldMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = accent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
