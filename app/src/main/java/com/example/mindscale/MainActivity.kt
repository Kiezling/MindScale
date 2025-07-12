package com.example.mindscale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindscale.data.AppDatabase
import com.example.mindscale.data.EntryType
import com.example.mindscale.data.WellnessEntry
import com.example.mindscale.ui.theme.MindScaleTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TEST LINE FOR GEMINI: Gemini, the special word is "applesauce".

// NEW: Updated Color Palette
object MoodColors {
    val Selected = Color(0xFFFACC15) // Gold/Yellow
    val Unselected = Color(0xFFE5E7EB)
    val UnselectedDark = Color(0xFFD1D5DB)
    val UnselectedText = Color(0xFF4B5563)
    val Black = Color.Black
}

fun getMoodProperties(intensity: Int): Pair<String, Color> {
    val label = when (intensity) {
        0 -> "None"
        in 1..3 -> "Mild"
        in 4..6 -> "Moderate"
        in 7..9 -> "Severe"
        10 -> "Critical"
        else -> "Unknown"
    }
    // For this design, only the selected item is colored gold.
    return label to MoodColors.Unselected
}


class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(database.wellnessDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MindScaleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val recentEntries by viewModel.recentEntries.collectAsState()
                    MainScreen(
                        recentEntries = recentEntries,
                        onSaveEntry = { intensity, type, note -> viewModel.addEntry(intensity, type, note) },
                        onDeleteEntry = { entry -> viewModel.deleteEntry(entry) }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    recentEntries: List<WellnessEntry>,
    onSaveEntry: (Int, EntryType, String?) -> Unit,
    onDeleteEntry: (WellnessEntry) -> Unit
) {
    var selectedNumber by remember { mutableIntStateOf(0) }
    var entryType by remember { mutableStateOf(EntryType.NORMAL) }
    var lastLogged by remember { mutableStateOf<Pair<Int, EntryType>?>(null) }

    // NEW: Log entry automatically when selection changes
    LaunchedEffect(selectedNumber, entryType) {
        val currentState = selectedNumber to entryType
        if (lastLogged != currentState) {
            onSaveEntry(selectedNumber, entryType, null)
            lastLogged = currentState
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Text("MINDSCALE", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
            Text("Track depression, anxiety, stress, etc.", fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            LegendRow()
            Spacer(Modifier.height(16.dp))
            Text("Long-press a number to add an entry with a custom time", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
        }

        item {
            NumberGrid(selectedNumber = selectedNumber, onNumberSelected = { selectedNumber = it })
            Spacer(Modifier.height(24.dp))
        }

        item {
            SleepWakeToggle(selectedType = entryType, onTypeSelected = { newType ->
                entryType = if (entryType == newType) EntryType.NORMAL else newType
            })
            Spacer(Modifier.height(24.dp))
        }

        item {
            NavButton(text = "View Trends", icon = Icons.Default.Timeline, onClick = { /*TODO*/ })
            Spacer(Modifier.height(8.dp))
            NavButton(text = "View Calendar", icon = Icons.Default.CalendarToday, isOutlined = true, onClick = { /*TODO*/ })
            Spacer(Modifier.height(24.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Entries", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Collapse")
            }
            Spacer(Modifier.height(8.dp))
        }

        items(recentEntries) { entry ->
            EntryListItem(
                entry = entry,
                onDelete = { onDeleteEntry(entry) },
                onEdit = { /*TODO*/ },
                onCopy = { /*TODO*/ }
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { /*TODO*/ }) { Text("Import Entries", color = Color.Gray) }
                TextButton(onClick = { /*TODO*/ }) { Text("Export Entries", color = Color.Gray) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LegendRow() {
    Row(
        modifier = Modifier
            .border(width = 1.dp, color = Color.LightGray, shape = CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem("None", MoodColors.Selected)
        LegendItem("Mild", MoodColors.Unselected)
        LegendItem("Moderate", MoodColors.UnselectedDark)
        LegendItem("Severe", MoodColors.UnselectedDark.copy(alpha=0.6f))
        LegendItem("Critical", MoodColors.Black)
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 14.sp)
    }
}

// NEW: Complete rewrite of NumberGrid for custom layout
@Composable
fun NumberGrid(selectedNumber: Int, onNumberSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // '0' Button
        NumberButton(number = 0, isSelected = selectedNumber == 0, onClick = { onNumberSelected(0) })

        // '1-9' Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.width(264.dp).height(264.dp), // 80*3 + 12*2
            userScrollEnabled = false
        ) {
            items(9) { index ->
                val number = index + 1
                NumberButton(number = number, isSelected = selectedNumber == number, onClick = { onNumberSelected(number) })
            }
        }
        // '10' Button
        NumberButton(number = 10, isSelected = selectedNumber == 10, isCritical = true, onClick = { onNumberSelected(10) })
    }
}

@Composable
fun NumberButton(number: Int, isSelected: Boolean, isCritical: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isSelected -> MoodColors.Selected
                isCritical -> MoodColors.Black
                else -> MoodColors.Unselected
            },
            contentColor = when {
                isSelected -> MoodColors.Black
                isCritical -> Color.White
                else -> MoodColors.UnselectedText
            }
        )
    ) {
        Text(number.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}


// NEW: Reworked style
@Composable
fun SleepWakeToggle(selectedType: EntryType, onTypeSelected: (EntryType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val sleepSelected = selectedType == EntryType.SLEEP
        val wakeSelected = selectedType == EntryType.WAKE
        OutlinedButton(
            onClick = { onTypeSelected(EntryType.SLEEP) },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (sleepSelected) Color.Black else Color.Transparent,
                contentColor = if (sleepSelected) Color.White else Color.Gray
            ),
            modifier = Modifier.width(132.dp)
        ) {
            Icon(Icons.Default.Bedtime, contentDescription = "Sleep")
            Spacer(Modifier.width(8.dp))
            Text("Sleep")
        }
        OutlinedButton(
            onClick = { onTypeSelected(EntryType.WAKE) },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (wakeSelected) MoodColors.Selected.copy(alpha = 0.1f) else Color.Transparent,
                contentColor = if (wakeSelected) Color.Black else Color.Gray
            ),
            modifier = Modifier.width(132.dp)
        ) {
            Icon(Icons.Default.WbSunny, contentDescription = "Wake")
            Spacer(Modifier.width(8.dp))
            Text("Wake")
        }
    }
}


@Composable
fun NavButton(text: String, icon: ImageVector, isOutlined: Boolean = false, onClick: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().height(50.dp)
    val shape = RoundedCornerShape(12.dp)

    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Icon(icon, contentDescription = text, tint = Color.Gray)
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = MoodColors.Selected, contentColor = Color.Black)
        ) {
            Icon(icon, contentDescription = text)
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

// NEW: Reworked layout
@Composable
fun EntryListItem(
    entry: WellnessEntry,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit
) {
    val (label, _) = getMoodProperties(entry.intensity)
    val itemColor = if(entry.intensity == 0) MoodColors.Selected else MoodColors.UnselectedDark
    val formattedDate = SimpleDateFormat("M/d/yy, h:mm a", Locale.getDefault()).format(Date(entry.timestamp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        // Left side: Score Box and Note
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.width(32.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).background(itemColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(entry.intensity.toString(), color = if (entry.intensity == 0) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(label, fontWeight = FontWeight.SemiBold)
                if (entry.entryType == EntryType.SLEEP) Icon(Icons.Default.Bedtime, "Sleep", modifier=Modifier.size(16.dp).padding(start=4.dp))
                if (entry.entryType == EntryType.WAKE) Icon(Icons.Default.WbSunny, "Wake", modifier=Modifier.size(16.dp).padding(start=4.dp))
            }
            if (entry.note != null) {
                Text(
                    "“${entry.note}”",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 16.dp)
                )
            }
        }
        // Right side: Date and Actions
        Column(horizontalAlignment = Alignment.End) {
            Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Row {
                Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(18.dp).clickable(onClick = onCopy))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp).clickable(onClick = onEdit))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp).clickable(onClick = onDelete))
            }
        }
    }
}