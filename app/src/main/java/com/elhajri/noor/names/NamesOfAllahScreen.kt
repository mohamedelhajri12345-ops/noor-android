package com.elhajri.noor.names

import com.elhajri.noor.ui.themeScreenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.data.DataLoader
import com.elhajri.noor.data.NameOfAllah
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.NoorGradients
import androidx.compose.ui.graphics.Color
import com.elhajri.noor.theme.noorGlassCard

@Composable
fun NamesOfAllahScreen() {
    val context = LocalContext.current
    val names = remember { DataLoader.namesOfAllah(context) }
    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<NameOfAllah?>(null) }

    Column(modifier = Modifier.fillMaxSize().themeScreenBackground().padding(16.dp)) {
        Text("أسماء الله الحسنى", color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("﴿وَلِلَّهِ الْأَسْمَاءُ الْحُسْنَىٰ فَادْعُوهُ بِهَا﴾", color = GoldSoft, fontSize = 14.sp, fontFamily = AmiriFamily)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("ابحث عن اسم...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        val filtered = if (search.isBlank()) names else names.filter { it.name.contains(search) || it.meaning.contains(search) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { name ->
                Card(
                    modifier = Modifier.aspectRatio(1f).clickable { selected = name }.noorGlassCard(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.radialGradient(listOf(NavyLight, NavyCard))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name.name, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = AmiriFamily, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

    selected?.let { name ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text("إغلاق", color = Gold) }
            },
            title = { Text(name.name, color = Gold, fontSize = 28.sp, fontFamily = AmiriFamily) },
            text = { Text(name.meaning, color = GoldSoft, fontSize = 16.sp, lineHeight = 26.sp) },
            containerColor = NavyCard
        )
    }
}
