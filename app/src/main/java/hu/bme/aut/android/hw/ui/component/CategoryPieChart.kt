package hu.bme.aut.android.hw.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.hw.domain.model.CategorySlice
import androidx.compose.foundation.layout.size   // ← add this
import androidx.compose.foundation.layout.width  // ← if you also use width()
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import androidx.compose.runtime.remember



@Composable
fun CategoryPieChart(
    slices: List<CategorySlice>,
    colors: List<Color>,
    modifier: Modifier = Modifier.size(240.dp),
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val total = slices.sumOf { it.amount.toDouble() }.takeIf { it > 0 } ?: 1.0
    Canvas(modifier) {
        var startAngle = -90f
        slices.forEachIndexed { idx, slice ->
            val sweep = ((slice.amount / total) * 360.0 * progress.value).toFloat()
            drawArc(
                color      = colors[idx],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter  = true,
                size       = size,
                topLeft    = Offset.Zero
            )
            startAngle += sweep
        }

    }
}

@Composable
fun CategoryLegend(
    slices: List<CategorySlice>,
    colors: List<Color>,
    defaultCurrency: String,
    rates: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalConverted = slices.sumOf { slice ->
        val rateFrom = rates[slice.currency] ?: 1.0
        val rateTo   = rates[defaultCurrency] ?: 1.0
        slice.amount * (rateTo / rateFrom)
    }.takeIf { it > 0 } ?: 1.0

    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {

        itemsIndexed(slices) { idx, slice ->
            val rateFrom = rates[slice.currency] ?: 1.0
            val rateTo = rates[defaultCurrency] ?: 1.0
            val convertedAmount = slice.amount * (rateTo / rateFrom)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(colors[idx], CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "%s: %.2f %s (%.1f%%)".format(
                        slice.category,
                        convertedAmount,
                        defaultCurrency,
                        convertedAmount / totalConverted * 100
                    )
                )
            }
        }
    }

}






