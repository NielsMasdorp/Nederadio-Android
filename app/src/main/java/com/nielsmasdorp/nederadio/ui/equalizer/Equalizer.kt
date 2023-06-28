package com.nielsmasdorp.nederadio.ui.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

@Composable
fun Equalizer(
    minBandRange: Float,
    maxBandRange: Float,
    equalizerProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Chart(
            isZoomEnabled = false,
            chart = lineChart(
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = minBandRange,
                    maxY = maxBandRange
                ),
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = MaterialTheme.colorScheme.secondaryContainer.toArgb(),
                        lineThicknessDp = 6f,
                        point = ShapeComponent(
                            shape = Shapes.pillShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.toArgb(),
                            strokeColor = MaterialTheme.colorScheme.onPrimary.toArgb(),
                            strokeWidthDp = 2f
                        )
                    )
                )
            ),
            chartModelProducer = equalizerProducer,
            bottomAxis = bottomAxis(
                label = textComponent {
                    this.color = MaterialTheme.colorScheme.onPrimary.toArgb()
                },
                valueFormatter = { value, chartValues ->
                    (
                        chartValues.chartEntryModel.entries.first()
                            .getOrNull(value.toInt()) as? EqualizerEntry
                        )?.hertz.orEmpty()
                }
            )
        )
    }
}
