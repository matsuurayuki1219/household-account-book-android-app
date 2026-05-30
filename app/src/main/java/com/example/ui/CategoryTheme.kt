package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

object CategoryTheme {
    val categories = listOf("食費", "日用品", "交通費", "交際費", "住宅・光熱", "趣味・娯楽", "給与", "その他")

    fun getColor(category: String): Color {
        return when (category) {
            "食費" -> ColorFood
            "日用品" -> ColorDaily
            "交通費" -> ColorTransport
            "交際費" -> ColorSocial
            "住宅・光熱" -> ColorHousing
            "趣味・娯楽" -> ColorHobbies
            "給与" -> ColorSalary
            else -> ColorOther
        }
    }

    fun getIcon(category: String): ImageVector {
        return when (category) {
            "食費" -> Icons.Default.Restaurant
            "日用品" -> Icons.Default.ShoppingBag
            "交通費" -> Icons.Default.DirectionsCar
            "交際費" -> Icons.Default.Celebration
            "住宅・光熱" -> Icons.Default.Home
            "趣味・娯楽" -> Icons.Default.SportsEsports
            "給与" -> Icons.Default.Payments
            else -> Icons.Default.Category
        }
    }
}
