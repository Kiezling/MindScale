package com.example.mindscale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mindscale.data.AppDatabase
import com.example.mindscale.data.WellnessEntry
import com.example.mindscale.ui.theme.AppColors
import com.example.mindscale.ui.theme.MindScaleTheme
import com.example.mindscale.ui.theme.Typography
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
                        onSaveEntry = { intensity, type, timestamp -> viewModel.addEntry(intensity, type, timestamp) },
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
    onSaveEntry: (Int, String, Long) -> Unit,
    onDeleteEntry: (WellnessEntry) -> Unit
) {
    var sleepMode by remember { mutableStateOf<String?>(null) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var selectedNumberForCustomEntry by remember { mutableStateOf<Int?>(null) }

    if (showDateTimePicker) {
        DateTimePickerModal(
            onDismiss = { showDateTimePicker = false },
            onSave = { timestamp ->
                val entryType = sleepMode ?: "depression"
                onSaveEntry(selectedNumberForCustomEntry!!, entryType, timestamp)
                showDateTimePicker = false
                sleepMode = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Text(
                "MINDSCALE",
                style = Typography.displayLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(AppColors.GrayDark, AppColors.Intensity5)
                    )
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Track depression, anxiety, stress, etc.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(Modifier.height(24.dp))
            LegendRow()
            Spacer(Modifier.height(16.dp))
            Text(
                "Long-press a number to add an entry with a custom time",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            val numpadModifier = sleepMode?.let {
                Modifier.border(BorderStroke(2.dp, Brush.linearGradient(listOf(AppColors.Turbo, AppColors.Gold))), RoundedCornerShape(16.dp))
            } ?: Modifier
            Column(numpadModifier) {
                NumberPad(
                    onNumberTap = { number ->
                        val entryType = sleepMode ?: "depression"
                        onSaveEntry(number, entryType, System.currentTimeMillis())
                        sleepMode = null
                    },
                    onNumberLongPress = { number ->
                        selectedNumberForCustomEntry = number
                        showDateTimePicker = true
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            SleepWakeToggle(
                activeMode = sleepMode,
                onModeSelected = { mode ->
                    sleepMode = if (sleepMode == mode) null else mode
                }
            )
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
            Spacer(Modifier.height(24.dp))
        }

        item {
            Text("Recent Entries", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
        }

        items(recentEntries) { entry ->
            EntryListItem(
                entry = entry,
                onDelete = { onDeleteEntry(entry) },
                onEdit = { /*TODO*/ },
                onEditNote = { /*TODO*/ }
            )
        }
    }
}

@Composable
fun LegendRow() {
    val legendItems = listOf(
        "None" to AppColors.Gold,
        "Mild" to AppColors.Intensity3,
        "Moderate" to AppColors.Intensity6,
        "Severe" to AppColors.Intensity8,
        "Critical" to AppColors.Intensity10
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        legendItems.forEach { (text, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text(text, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun DateTimePickerModal(onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    var calendarState by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US)
    var showMonthYearPicker by remember { mutableStateOf(false) }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            initialCalendar = calendarState,
            onDismiss = { showMonthYearPicker = false },
            onMonthYearSelected = { month, year ->
                calendarState = (calendarState.clone() as Calendar).apply {
                    set(Calendar.MONTH, month)
                    set(Calendar.YEAR, year)
                }
                showMonthYearPicker = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(calendarState.time),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black
                    )
                )
                Spacer(Modifier.height(8.dp))
                CalendarView(
                    calendar = calendarState,
                    onDateSelected = { day ->
                        calendarState = (calendarState.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                    },
                    onMonthYearClick = { showMonthYearPicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TimePickerView(
                    calendar = calendarState,
                    onTimeSelected = { hour, minute, isPM ->
                        calendarState = (calendarState.clone() as Calendar).apply {
                            set(Calendar.HOUR, if (hour == 12) 0 else hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.AM_PM, if (isPM) Calendar.PM else Calendar.AM)
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonModifier = Modifier.height(40.dp).width(120.dp)
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        modifier = buttonModifier
                    ) {
                        Text("Cancel", color = Color.Black)
                    }
                    OutlinedButton(
                        onClick = { onSave(calendarState.timeInMillis) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = buttonModifier,
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(AppColors.Turbo, AppColors.Gold))),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)

                    ) {
                        Text("Save", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(calendar: Calendar, onDateSelected: (Int) -> Unit, onMonthYearClick: () -> Unit, modifier: Modifier = Modifier) {
    var displayCalendar by remember(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) {
        mutableStateOf(calendar.clone() as Calendar)
    }
    val monthFormatter = SimpleDateFormat("MMMM, yyyy", Locale.US)

    val daysInMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (displayCalendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK)
    val emptyDays = firstDayOfWeek - 1

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { displayCalendar = (displayCalendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) {
                Icon(Icons.AutoMirrored.Filled.ArrowLeft, "Previous Month")
            }
            Text(
                text = monthFormatter.format(displayCalendar.time),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onMonthYearClick() }
                    .padding(8.dp)
            )
            IconButton(onClick = { displayCalendar = (displayCalendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) {
                Icon(Icons.AutoMirrored.Filled.ArrowRight, "Next Month")
            }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(7)) {
            val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            items(days.size) {
                Text(days[it], textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false) {
            items(emptyDays) { Box(Modifier.size(40.dp)) }
            items(daysInMonth) { day ->
                val dayNumber = day + 1
                val isSelected = dayNumber == calendar.get(Calendar.DAY_OF_MONTH) &&
                        displayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        displayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) AppColors.Gold else Color.Transparent)
                        .clickable { onDateSelected(dayNumber) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber.toString(),
                        color = if (isSelected) Color.Black else Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TimePickerView(calendar: Calendar, onTimeSelected: (Int, Int, Boolean) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val currentHour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentIsPM = calendar.get(Calendar.AM_PM) == Calendar.PM

    val hours = (1..12).toList()
    val minutes = (0..59).toList()

    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hours.indexOf(currentHour))
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minutes.indexOf(currentMinute))

    LaunchedEffect(currentHour) { coroutineScope.launch { hourState.animateScrollToItem(hours.indexOf(currentHour)) } }
    LaunchedEffect(currentMinute) { coroutineScope.launch { minuteState.animateScrollToItem(minutes.indexOf(currentMinute)) } }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hour", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            LazyRow(
                state = hourState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(hours) { hour ->
                    Text(
                        text = hour.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (hour == currentHour) 20.sp else 16.sp,
                        color = if (hour == currentHour) AppColors.Gold else Color.Gray,
                        modifier = Modifier
                            .clickable { onTimeSelected(hour, currentMinute, currentIsPM) }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Minute", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            LazyRow(
                state = minuteState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(minutes) { minute ->
                    Text(
                        text = minute.toString().padStart(2, '0'),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (minute == currentMinute) 20.sp else 16.sp,
                        color = if (minute == currentMinute) AppColors.Gold else Color.Gray,
                        modifier = Modifier
                            .clickable { onTimeSelected(currentHour, minute, currentIsPM) }
                            .padding(4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val buttonModifier = Modifier.width(80.dp).height(32.dp)
                AmPmButton(
                    label = "AM",
                    isSelected = !currentIsPM,
                    onClick = { onTimeSelected(currentHour, currentMinute, false) },
                    modifier = buttonModifier.weight(1f, fill = false)
                )
                Spacer(Modifier.weight(0.5f))
                AmPmButton(
                    label = "PM",
                    isSelected = currentIsPM,
                    onClick = { onTimeSelected(currentHour, currentMinute, true) },
                    modifier = buttonModifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@Composable
fun AmPmButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderBrush = if (isSelected) {
        Brush.linearGradient(listOf(AppColors.Turbo, AppColors.Gold))
    } else {
        SolidColor(Color.Black)
    }
    val textColor = if (isSelected) AppColors.Gold else Color.Black


    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderBrush),
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = textColor)
        }
    }
}


@Composable
fun MonthYearPickerDialog(
    initialCalendar: Calendar,
    onDismiss: () -> Unit,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    val currentYear = initialCalendar.get(Calendar.YEAR)
    val currentMonth = initialCalendar.get(Calendar.MONTH)

    val years = (currentYear - 100..currentYear + 100).toList()
    val months = (0..11).map {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, it)
        SimpleDateFormat("MMMM", Locale.US).format(cal.time)
    }

    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }

    val yearState = rememberLazyListState(initialFirstVisibleItemIndex = years.indexOf(currentYear))
    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = currentMonth)

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.height(200.dp)) {
                    LazyColumn(state = monthState, modifier = Modifier.weight(1f)) {
                        items(months.size) { index ->
                            Text(
                                text = months[index],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMonth = index }
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (selectedMonth == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedMonth == index) AppColors.Gold else Color.Black
                            )
                        }
                    }
                    LazyColumn(state = yearState, modifier = Modifier.weight(1f)) {
                        items(years.size) { index ->
                            Text(
                                text = years[index].toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedYear = years[index] }
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = if (selectedYear == years[index]) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedYear == years[index]) AppColors.Gold else Color.Black
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Black) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onMonthYearSelected(selectedMonth, selectedYear) }) { Text("OK", color = Color.Black) }
                }
            }
        }
    }
}


@Composable
fun NumberPad(onNumberTap: (Int) -> Unit, onNumberLongPress: (Int) -> Unit) {
    val buttonSize = 64.dp
    val buttonShape = RoundedCornerShape(12.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // Row 0
        NumberButton(number = 0, shape = buttonShape, size = buttonSize, onNumberTap = onNumberTap, onNumberLongPress = onNumberLongPress)
        // Rows 1-3
        (1..3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..3).forEach { col ->
                    val number = (row - 1) * 3 + col
                    NumberButton(number = number, shape = buttonShape, size = buttonSize, onNumberTap = onNumberTap, onNumberLongPress = onNumberLongPress)
                }
            }
        }
        // Row 10
        NumberButton(number = 10, shape = buttonShape, size = buttonSize, onNumberTap = onNumberTap, onNumberLongPress = onNumberLongPress)
    }
}

@Composable
fun NumberButton(
    number: Int,
    shape: RoundedCornerShape,
    size: Dp,
    onNumberTap: (Int) -> Unit,
    onNumberLongPress: (Int) -> Unit
) {
    val gradient = getIntensityGradient(number)
    // UPDATED: Text is white unless the number is 0
    val textColor = if (number == 0) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(gradient)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onNumberTap(number) },
                    onLongPress = { onNumberLongPress(number) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(number.toString(), color = textColor, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)
    }
}

@Composable
fun SleepWakeToggle(activeMode: String?, onModeSelected: (String) -> Unit) {
    // UPDATED: Adjust the height of the buttons here
    val buttonModifier = Modifier
        .width(132.dp)
        .height(48.dp)

    val sleepButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.White
    )

    val goldBorder = BorderStroke(2.dp, Brush.linearGradient(listOf(AppColors.Turbo, AppColors.Gold)))

    val lightFactor = 1.15f
    val lightGrayWake = Color(
        red = (AppColors.GrayWake.red * lightFactor).coerceIn(0f, 1f),
        green = (AppColors.GrayWake.green * lightFactor).coerceIn(0f, 1f),
        blue = (AppColors.GrayWake.blue * lightFactor).coerceIn(0f, 1f)
    )

    val wakeButtonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = Color.Black
    )

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Sleep Button
        Button(
            onClick = { onModeSelected("sleep") },
            shape = RoundedCornerShape(8.dp),
            modifier = buttonModifier,
            contentPadding = PaddingValues(),
            colors = sleepButtonColors,
            border = if (activeMode == "sleep") goldBorder else null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                AppColors.GrayLight,
                                AppColors.GrayDark
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Nightlight, contentDescription = "Sleep")
                    Spacer(Modifier.width(8.dp))
                    Text("Sleep")
                }
            }
        }

        // Wake Button
        Button(
            onClick = { onModeSelected("wake") },
            shape = RoundedCornerShape(8.dp),
            modifier = buttonModifier,
            colors = wakeButtonColors,
            contentPadding = PaddingValues(),
            border = if (activeMode == "wake") goldBorder else null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                lightGrayWake,
                                AppColors.GrayWake
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, contentDescription = "Wake")
                    Spacer(Modifier.width(8.dp))
                    Text("Wake")
                }
            }
        }
    }
}


@Composable
fun EntryListItem(
    entry: WellnessEntry,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onEditNote: () -> Unit
) {
    val formattedDate = SimpleDateFormat("M/d/yy, h:mm a", Locale.getDefault()).format(Date(entry.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Intensity Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(getIntensityGradient(entry.intensity ?: -1)),
            contentAlignment = Alignment.Center
        ) {
            if (entry.intensity != null) {
                Text(
                    entry.intensity.toString(),
                    color = if (entry.intensity >= 7) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(8.dp))

        // Sleep/Wake Icon
        when (entry.type) {
            "sleep" -> Icon(Icons.Default.Nightlight, "Sleep", tint = Color.Gray)
            "wake" -> Icon(Icons.Default.WbSunny, "Wake", tint = Color.Gray)
        }
        Spacer(Modifier.width(8.dp))

        // Date and Time
        Text(
            text = formattedDate,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )

        // Action Icons
        Row {
            IconButton(onClick = onEditNote) {
                Icon(Icons.Default.Description, "Edit Note", tint = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit Entry", tint = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete Entry", tint = Color.Gray)
            }
        }
    }
}


fun getIntensityGradient(intensity: Int): Brush {
    val lightFactor = 1.35f
    val lighten = { color: Color ->
        val red = (color.red * lightFactor).coerceIn(0f, 1f)
        val green = (color.green * lightFactor).coerceIn(0f, 1f)
        val blue = (color.blue * lightFactor).coerceIn(0f, 1f)
        Color(red, green, blue)
    }

    val (startColor, endColor) = when (intensity) {
        0 -> AppColors.Turbo to AppColors.Gold
        1 -> lighten(AppColors.Intensity1) to AppColors.Intensity1
        2 -> lighten(AppColors.Intensity2) to AppColors.Intensity2
        3 -> lighten(AppColors.Intensity3) to AppColors.Intensity3
        4 -> lighten(AppColors.Intensity4) to AppColors.Intensity4
        5 -> lighten(AppColors.Intensity5) to AppColors.Intensity5
        6 -> lighten(AppColors.Intensity6) to AppColors.Intensity6
        7 -> lighten(AppColors.Intensity7) to AppColors.Intensity7
        8 -> lighten(AppColors.Intensity8) to AppColors.Intensity8
        9 -> lighten(AppColors.Intensity9) to AppColors.Intensity9
        10 -> lighten(AppColors.Intensity10) to AppColors.Intensity10
        else -> Color.LightGray to Color.Gray
    }
    return Brush.linearGradient(listOf(startColor, endColor))
}