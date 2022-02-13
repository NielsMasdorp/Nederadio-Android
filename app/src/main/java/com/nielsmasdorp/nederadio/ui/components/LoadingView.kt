package com.nielsmasdorp.nederadio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.*
import com.nielsmasdorp.nederadio.R

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    foregroundColor: Color
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
        val progress by animateLottieCompositionAsState(composition)
        val dynamicProperties = rememberLottieDynamicProperties(
            rememberLottieDynamicProperty(
                property = LottieProperty.COLOR,
                value = Color.Red.toArgb(),
                keyPath = arrayOf(
                    "播放动画",
                    "路径 1",
                    "形状图层 20",
                    "填充 1",
                    "变换",
                    "矩形 1",
                    "形状图层 19",
                    "形状图层 18",
                    "填充 1",
                    "变换",
                    "矩形 1"
                )
            )
        ) // TODO fix color
        LottieAnimation(
            modifier = Modifier
                .size(192.dp)
                .align(Alignment.Center),
            composition = composition,
            progress = progress,
            dynamicProperties = dynamicProperties
        )
    }
}

@Composable
@Preview
fun LoadingViewPreview() {
    LoadingView(backgroundColor = Color.Black, foregroundColor = Color.White)
}