package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.CategoryTheme
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GridLineDarkGray
import com.example.ui.theme.GridLineGray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MinimalDonutChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val expenses = transactions.filter { it.type == "EXPENSE" }
    val totalExpense = expenses.sumOf { it.amount }

    // Group transactions by category and calculate sum
    val categorySum = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }
    var animationTrigger by remember { mutableStateOf(false) }
    
    LaunchedEffect(transactions) {
        animationTrigger = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "支出のカテゴリ内訳",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (totalExpense == 0L) {
                // Empty State Donut
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "¥0",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = "データなし",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                // Non-empty Chart layout (adaptive Row/Column)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Custom Donut canvas
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val strokeWidthPx = with(LocalDensity.current) { 24.dp.toPx() }
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            categorySum.forEach { (category, amount) ->
                                val percentage = amount.toFloat() / totalExpense
                                val sweepAngle = percentage * 360f * animatedProgress
                                
                                // Draw actual segment with 2-degree visual gap
                                if (sweepAngle > 3f) {
                                    drawArc(
                                        color = CategoryTheme.getColor(category),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle - 2f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidthPx)
                                    )
                                } else if (sweepAngle > 0f) {
                                    drawArc(
                                        color = CategoryTheme.getColor(category),
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidthPx)
                                    )
                                }
                                startAngle += sweepAngle
                            }
                        }

                        // Text in center
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "支出合計",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatter.format(totalExpense),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legends block
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        categorySum.take(5).forEach { (category, amount) ->
                            val percent = (amount.toDouble() / totalExpense * 100).toInt()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(CategoryTheme.getColor(category), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$percent%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (categorySum.size > 5) {
                            val otherSum = categorySum.drop(5).sumOf { it.second }
                            val percent = (otherSum.toDouble() / totalExpense * 100).toInt()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "その他",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$percent%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
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
fun MinimalBarTrendChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.let { it.red < 0.2f && it.green < 0.2f && it.blue < 0.2f }
    val lineGridColor = if (isDark) GridLineDarkGray else GridLineGray

    // Get expenses of past 7 days
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("M/d", Locale.JAPAN)
    
    // Set to 7 days
    val last7Days = remember(transactions) {
        val daysList = mutableListOf<Pair<String, Long>>()
        val tempCal = Calendar.getInstance()
        
        // Let's create an ordered list of last 7 calendar days
        for (i in 6 downTo 0) {
            val d = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            // Parse day start and end
            val startOfDay = d.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = d.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis

            val dayLabel = sdf.format(d.time)
            
            // Sum expenses on this day
            val daySum = transactions.filter { 
                it.type == "EXPENSE" && it.timestamp in startOfDay..endOfDay 
            }.sumOf { it.amount }

            daysList.add(Pair(dayLabel, daySum))
        }
        daysList
    }

    val maxAmount = remember(last7Days) {
        val max = last7Days.maxOf { it.second }
        if (max == 0L) 10000L else max
    }

    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(transactions) {
        animationTrigger = true
    }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    val formatter = remember { NumberFormat.getIntegerInstance(Locale.JAPAN) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "直近7日間の支出推移",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Graph visualization with Gridlines
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Background grid lines and y-axis labels
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val levels = listOf(maxAmount, maxAmount / 2, 0L)
                    levels.forEach { value ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¥${formatter.format(value)}",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp,
                                modifier = Modifier.width(56.dp)
                            )
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                            ) {
                                drawLine(
                                    color = lineGridColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                    }
                }

                // Vertical solid bars
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 56.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    last7Days.forEach { (label, amount) ->
                        // Calculate percentage of height
                        val barHeightFactor = amount.toFloat() / maxAmount
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // The Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.8f * barHeightFactor * animatedProgress)
                                    .width(16.dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (amount > 0) ExpenseCoral else MaterialTheme.colorScheme.secondary.copy(
                                            alpha = 0.15f
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Label of date
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
