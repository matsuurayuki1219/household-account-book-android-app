package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Transaction
import com.example.ui.BudgetViewModel
import com.example.ui.CategoryTheme
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeTeal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BudgetViewModel,
    isDark: Boolean,
    onToggleDark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val typeFilter by viewModel.selectedTypeFilter.collectAsState()
    val activeTab by viewModel.chartsTab.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Computations
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }
    val dateFormatter = remember { SimpleDateFormat("M/d HH:mm", Locale.JAPAN) }

    // Filter transactions
    val filteredTransactions = remember(transactions, typeFilter) {
        when (typeFilter) {
            "EXPENSE" -> transactions.filter { it.type == "EXPENSE" }
            "INCOME" -> transactions.filter { it.type == "INCOME" }
            else -> transactions
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "取引を追加",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // LazyColumn to lay out elements and remain fully scrollable for compact screen heights
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                // Top Custom App Bar Row (No standard default purple bar)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KAKEIBO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "ミニマル家計簿",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Theme Mode Selector Button
                        IconButton(
                            onClick = onToggleDark,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.DarkMode,
                                contentDescription = "テーマ切替",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Balance Summary Display Block Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "現在の総資産残高",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currencyFormatter.format(netBalance),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = if (netBalance >= 0) MaterialTheme.colorScheme.primary else ExpenseCoral,
                                letterSpacing = (-1).sp
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Side-by-side Income / Expense Summary Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Income
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(IncomeTeal.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "収入",
                                            tint = IncomeTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "収入合計",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currencyFormatter.format(totalIncome),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeTeal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Space Divider
                                Spacer(modifier = Modifier.width(16.dp))

                                // Expense
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ExpenseCoral.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "支出",
                                            tint = ExpenseCoral,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "支出合計",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currencyFormatter.format(totalExpense),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseCoral,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chart Tabs Switcher
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        // Donut Tab Code
                        val donutBg by animateColorAsState(
                            targetValue = if (activeTab == 0) MaterialTheme.colorScheme.background else Color.Transparent
                        )
                        val donutContentColor by animateColorAsState(
                            targetValue = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .background(donutBg)
                                .clickable { viewModel.setChartsTab(0) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = "内訳",
                                    tint = donutContentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "カテゴリ内訳",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = donutContentColor
                                )
                            }
                        }

                        // Bar Tab Code
                        val barBg by animateColorAsState(
                            targetValue = if (activeTab == 1) MaterialTheme.colorScheme.background else Color.Transparent
                        )
                        val barContentColor by animateColorAsState(
                            targetValue = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .background(barBg)
                                .clickable { viewModel.setChartsTab(1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "推移",
                                    tint = barContentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "日付推移 (7日間)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = barContentColor
                                )
                            }
                        }
                    }
                }

                // Render Active Chart
                item {
                    AnimatedVisibility(
                        visible = activeTab == 0,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MinimalDonutChart(transactions = transactions)
                    }
                    AnimatedVisibility(
                        visible = activeTab == 1,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MinimalBarTrendChart(transactions = transactions)
                    }
                }

                // Transaction Feed Section Header and Filters row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "取引履歴",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Filters layout (All / Ex / In)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val filterOptions = listOf(
                                Triple("ALL", "全体", MaterialTheme.colorScheme.primary),
                                Triple("EXPENSE", "支出", ExpenseCoral),
                                Triple("INCOME", "収入", IncomeTeal)
                            )

                            filterOptions.forEach { (typeKey, label, color) ->
                                val isSelected = typeFilter == typeKey
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) color.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable { viewModel.setTypeFilter(typeKey) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Feed display list
                if (filteredTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.background, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "取引なし",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "条件に合う履歴はありません",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "右下の ＋ ボタンから追加してください",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { item ->
                        val isExpense = item.type == "EXPENSE"
                        val categoryColor = CategoryTheme.getColor(item.category)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category color icon bubble
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryTheme.getIcon(item.category),
                                        contentDescription = item.category,
                                        tint = categoryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Item details title and time
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dateFormatter.format(Date(item.timestamp)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        if (item.memo.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "•  ${item.memo}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Currency amount indicator
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = if (isExpense) "-${currencyFormatter.format(item.amount)}" 
                                               else "+${currencyFormatter.format(item.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isExpense) ExpenseCoral else IncomeTeal
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Subtle Delete action icon
                                IconButton(
                                    onClick = { viewModel.deleteTransaction(item) },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background, CircleShape)
                                        .size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "削除",
                                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overlay form if active
            if (showAddDialog) {
                AddTransactionDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = { title, amount, type, category, memo ->
                        viewModel.insertTransaction(title, amount, type, category, memo)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}
