package com.elhajri.noor.zakat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.NoorGradients

private fun toArabicDigits(number: Any): String {
    val str = number.toString()
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return str.map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

private val KARAT_PURITY = mapOf(
    "24" to 0.999,
    "22" to 0.916,
    "21" to 0.875,
    "18" to 0.750
)

private data class ZakatCategory(
    val id: String,
    val label: String,
    val icon: String,
    val desc: String
)

private data class ZakatResult(
    val value: Double,
    val nisabValue: Double,
    val above: Boolean,
    val zakat: Double,
    val label: String
)

@Composable
fun ZakatScreen() {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<ZakatResult?>(null) }

    val categories = remember {
        listOf(
            ZakatCategory("gold", "الذهب", "🥇", "زكاة الذهب حسب العيار"),
            ZakatCategory("silver", "الفضة", "🥈", "زكاة الفضة"),
            ZakatCategory("cash", "النقود", "💵", "زكاة النقود والمدخرات"),
            ZakatCategory("merch", "المعروضات", "📦", "زكاة البضائع التجارية"),
            ZakatCategory("mixed", "المحصّل", "🧮", "حساب شامل لكل ما تملك")
        )
    }

    var goldGrams by remember { mutableStateOf("") }
    var goldKarat by remember { mutableStateOf("21") }
    var goldPrice by remember { mutableStateOf("70") }

    var silverGrams by remember { mutableStateOf("") }
    var silverPrice by remember { mutableStateOf("0.87") }

    var cash by remember { mutableStateOf("") }
    var merchValue by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    fun calculate() {
        result = null
        val gPrice = goldPrice.toDoubleOrNull() ?: 70.0
        val sPrice = silverPrice.toDoubleOrNull() ?: 0.87

        when (selectedCategory) {
            "gold" -> {
                val grams = goldGrams.toDoubleOrNull() ?: 0.0
                val purity = KARAT_PURITY[goldKarat] ?: 0.875
                val pureGrams = grams * purity
                val nisab = 85.0
                val valAmount = pureGrams * gPrice
                val nisabVal = nisab * gPrice
                val above = pureGrams >= nisab
                result = ZakatResult(valAmount, nisabVal, above, if (above) valAmount * 0.025 else 0.0, "الذهب")
            }
            "silver" -> {
                val grams = silverGrams.toDoubleOrNull() ?: 0.0
                val valAmount = grams * sPrice
                val nisab = 595.0
                val nisabVal = nisab * sPrice
                val above = grams >= nisab
                result = ZakatResult(valAmount, nisabVal, above, if (above) valAmount * 0.025 else 0.0, "الفضة")
            }
            "cash" -> {
                val amount = cash.toDoubleOrNull() ?: 0.0
                val nisabVal = 85.0 * gPrice
                val above = amount >= nisabVal
                result = ZakatResult(amount, nisabVal, above, if (above) amount * 0.025 else 0.0, "النقود")
            }
            "merch" -> {
                val amount = merchValue.toDoubleOrNull() ?: 0.0
                val nisabVal = 85.0 * gPrice
                val above = amount >= nisabVal
                result = ZakatResult(amount, nisabVal, above, if (above) amount * 0.025 else 0.0, "المعروضات")
            }
            "mixed" -> {
                val gGrams = goldGrams.toDoubleOrNull() ?: 0.0
                val purity = KARAT_PURITY[goldKarat] ?: 0.875
                val gVal = gGrams * purity * gPrice
                val sVal = (silverGrams.toDoubleOrNull() ?: 0.0) * sPrice
                val cVal = cash.toDoubleOrNull() ?: 0.0
                val mVal = merchValue.toDoubleOrNull() ?: 0.0
                val total = gVal + sVal + cVal + mVal
                val nisabVal = 85.0 * gPrice
                val above = total >= nisabVal
                result = ZakatResult(total, nisabVal, above, if (above) total * 0.025 else 0.0, "المحصّل")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoorGradients.ScreenBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "حاسبة الزكاة",
                style = MaterialTheme.typography.headlineMedium,
                color = Gold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "اختر نوع المال الذي تريد حساب زكاته",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldSoft
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedCategory == null) {
            // Category list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.forEach { c ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = c.id
                                result = null
                            },
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Gold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(c.icon, fontSize = 22.sp)
                                }

                                Column {
                                    Text(
                                        text = c.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = c.desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GoldSoft.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = GoldSoft
                            )
                        }
                    }
                }
            }
        } else {
            // Inputs View
            TextButton(
                onClick = {
                    selectedCategory = null
                    result = null
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Gold)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("رجوع للاختيار", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            val cat = selectedCategory

            if (cat == "gold" || cat == "mixed") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الذهب", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("الوزن (غرام)", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = goldGrams,
                            onValueChange = { goldGrams = it },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Gold.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("العيار", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("18", "21", "22", "24").forEach { k ->
                                val isSelected = goldKarat == k
                                Button(
                                    onClick = { goldKarat = k },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Gold else NavyLight,
                                        contentColor = if (isSelected) Navy else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(toArabicDigits(k), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("سعر الغرام (دولار)", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = goldPrice,
                            onValueChange = { goldPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Gold.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (cat == "silver" || cat == "mixed") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الفضة", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("الوزن (غرام)", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = silverGrams,
                            onValueChange = { silverGrams = it },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Gold.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("سعر الغرام (دولار)", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = silverPrice,
                            onValueChange = { silverPrice = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Gold.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (cat == "cash" || cat == "mixed") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("النقود", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cash,
                            onValueChange = { cash = it },
                            placeholder = { Text("المبلغ (دولار)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Gold.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (cat == "merch" || cat == "mixed") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("المعروضات (البضائع التجارية)", style = MaterialTheme.typography.titleMedium, color = Gold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = merchValue,
                            onValueChange = { merchValue = it },
                            placeholder = { Text("قيمة البضاعة (دولار)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Gold.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { calculate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("احسب الزكاة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            result?.let { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قيمة ${res.label}", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
                            Text("$${toArabicDigits(String.format("%.2f", res.value))}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("النصاب", style = MaterialTheme.typography.bodyMedium, color = GoldSoft)
                            Text("$${toArabicDigits(String.format("%.2f", res.nisabValue))}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (res.above) Gold.copy(alpha = 0.15f) else NavyLight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (res.above) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                        Text("مالك يبلغ النصاب", style = MaterialTheme.typography.titleSmall, color = Gold, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "$${toArabicDigits(String.format("%.2f", res.zakat))}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Gold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("زكاتك المستحقة (٢٫٥٪)", style = MaterialTheme.typography.bodySmall, color = GoldSoft)
                                } else {
                                    Text(
                                        "مالك لا يبلغ النصاب، لا زكاة عليك",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GoldSoft,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
