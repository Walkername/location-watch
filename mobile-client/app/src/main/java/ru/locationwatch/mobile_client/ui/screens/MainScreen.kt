package ru.locationwatch.mobile_client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    statusText: MutableState<String>,
    latitude: MutableState<String>,
    longitude: MutableState<String>,
    speed: MutableState<String>,
    startPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorStops = arrayOf(
        0.1f to Color.White,
        0.6f to Color(0xFF7EE882),
        1f to Color(0xFF1EE1AE)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f),
                    colorStops = colorStops
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(5f)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .height(400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Latitude: "
                    )
                    Text(
                        text = latitude.value
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Longitude: "
                    )
                    Text(
                        text = longitude.value
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Speed: "
                    )
                    Text(
                        text = speed.value
                    )
                }
            }
            Row(
                modifier = Modifier
                    .width(300.dp)
                    .height(50.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText.value
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(bottom = 80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .size(120.dp),
                onClick = { startPublish() }
            ) {
                Text("Start")
            }
        }
    }
}