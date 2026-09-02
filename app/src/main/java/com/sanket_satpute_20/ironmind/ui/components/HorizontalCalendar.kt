package com.sanket_satpute_20.ironmind.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import com.sanket_satpute_20.ironmind.ui.theme.SurfaceElevated
import com.sanket_satpute_20.ironmind.ui.theme.ErrorRed

@Composable
fun HorizontalCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val dateRange = remember {
        val today = LocalDate.now()
        (-7..14).map { today.plusDays(it.toLong()) }
    }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Auto-scroll to Today on first load
    LaunchedEffect(Unit) {
        val todayIndex = dateRange.indexOf(LocalDate.now())
        if (todayIndex != -1) {
            listState.scrollToItem(todayIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        itemsIndexed(dateRange) {
            index, date ->
            DateCard(
                date = date,
                isSelected = date == selectedDate,
                onDateSelected = {
                    onDateSelected(it)
                    scope.launch {
                        listState.animateScrollToItem(index)
                    }
                }
            )
        }
    }
}

@Composable
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
    val dayOfMonth = date.dayOfMonth.toString()
    val isToday = date == LocalDate.now()

    Surface(
        onClick = { onDateSelected(date) },
        color = if (isSelected) ErrorRed else SurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = if (isToday && !isSelected) BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)) else null,
        modifier = Modifier.size(width = 64.dp, height = 80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                dayName.uppercase(),
                fontSize = 11.sp,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                dayOfMonth,
                fontSize = 20.sp,
                color = if (isSelected) Color.White else Color.LightGray,
                fontWeight = FontWeight.Black
            )
            
            // Placeholder for success/fail dot
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.size(6.dp).background(Color.Transparent, CircleShape))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun HorizontalCalendarPreview() {
    com.sanket_satpute_20.ironmind.ui.theme.IronMindTheme {
        HorizontalCalendar(selectedDate = java.time.LocalDate.now(), onDateSelected = {})
    }
}
