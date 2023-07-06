package com.nielsmasdorp.nederadio.ui.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.BrushShader
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer

@Suppress("MagicNumber")
@Composable
fun Equalizer(
    minBandRange: Float,
    maxBandRange: Float,
    equalizerProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Chart(
            runInitialAnimation = false,
            isZoomEnabled = false,
            chart = lineChart(
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = minBandRange,
                    maxY = maxBandRange
                ),
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = MaterialTheme.colorScheme.tertiary.toArgb(),
                        lineThicknessDp = 6f,
                        point = ShapeComponent(
                            shape = Shapes.pillShape,
                            color = MaterialTheme.colorScheme.tertiary.toArgb(),
                            strokeColor = MaterialTheme.colorScheme.tertiaryContainer.toArgb(),
                            strokeWidthDp = 2f
                        ),
                        lineBackgroundShader = BrushShader(
                            verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary.copy(0.5f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0f)
                                ),
                            )
                        ),
                    )
                )
            ),
            chartModelProducer = equalizerProducer,
            bottomAxis = bottomAxis(
                label = textComponent {
                    this.color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
                },
                guideline = lineComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    thickness = currentChartStyle.axis.axisGuidelineWidth,
                    shape = currentChartStyle.axis.axisGuidelineShape
                ),
                tickPosition = HorizontalAxis.TickPosition.Center(),
                tick = lineComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    thickness = currentChartStyle.axis.axisTickWidth,
                    shape = currentChartStyle.axis.axisTickShape
                ),
                axis = lineComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    thickness = currentChartStyle.axis.axisLineWidth,
                    shape = currentChartStyle.axis.axisLineShape
                ),
                valueFormatter = { value, chartValues ->
                    val entries = chartValues.chartEntryModel.entries.first()
                    val entry: EqualizerEntry = entries[value.toInt()] as EqualizerEntry
                    entry.hertz
                }
            )
        )
    }
}
