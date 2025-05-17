package hu.bme.aut.android.hw.ui.screen.transactions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.*


@Composable
fun MoneyRainOverlay(
    visible: Boolean,
    onFinished: () -> Unit
) {
    if (!visible) return

    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("money_rain.json")
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f,
        restartOnPlay = false,
        cancellationBehavior = LottieCancellationBehavior.OnIterationFinish
    )

    LaunchedEffect(progress) {
        if (progress >= 0.999f) {
            onFinished()
        }
    }

    Box(
        Modifier
            .fillMaxSize(),
            //.background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress    = { progress },
            modifier    = Modifier.fillMaxSize(0.8f)
        )
    }
}


