package ru.locationwatch.mobile_client.ui.screens

import android.app.Application
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import ru.locationwatch.mobile_client.ui.AuthViewModel
import ru.locationwatch.mobile_client.ui.UserUiState
import ru.locationwatch.mobile_client.ui.UserViewModel
import ru.locationwatch.mobile_client.ui.theme.MobileclientTheme

@Composable
fun MainScreen(
    statusText: MutableState<String>,
    latitude: MutableState<String>,
    longitude: MutableState<String>,
    speed: MutableState<String>,
    startPublish: () -> Unit,
    navigateToAuth: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val userViewModelFactory = UserViewModel.createFactory(app)
    val userViewModel: UserViewModel = viewModel(factory = userViewModelFactory)

    val authViewModelFactory = AuthViewModel.createFactory(app)
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

    LaunchedEffect(Unit) {
        authViewModel.loadUserId()
        userViewModel.fetchUser(authViewModel.userId!!)
    }

    val userUiState = userViewModel.userUiState

    val username = remember {
        mutableStateOf("")
    }

    when (userUiState) {
        is UserUiState.Loading -> {
        }

        is UserUiState.Success -> {
            val userName: String = if (userUiState.user.username != null) userUiState.user.username!! else ""
            username.value = userName
        }

        is UserUiState.Error -> {
        }
    }

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
        Button(
            modifier = Modifier
                .padding(start = 10.dp, top = 10.dp),
            onClick = { navigateToAuth() }
        ) {
            Text(
                text = "Exit"
            )
        }
        Column(
            modifier = Modifier
                .weight(5f)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .height(500.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = username.value,
                        fontSize = 24.sp
                    )
                }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                ) {
//                    Text(
//                        text = "Latitude: "
//                    )
//                    Text(
//                        text = latitude.value
//                    )
//                }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                ) {
//                    Text(
//                        text = "Longitude: "
//                    )
//                    Text(
//                        text = longitude.value
//                    )
//                }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                ) {
//                    Text(
//                        text = "Speed: "
//                    )
//                    Text(
//                        text = speed.value
//                    )
//                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(700.dp)  // Fixed height for map container
                        .padding(8.dp)
                ) {
                    OpenStreetMap(
                        modifier = Modifier
                            .fillMaxSize(),
                        initialPosition = GeoPoint(59.937500, 30.308611)
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

@Composable
fun OpenStreetMap(
    modifier: Modifier = Modifier,
    initialPosition: GeoPoint = GeoPoint(59.937500, 30.308611),
    zoomLevel: Double = 12.0
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply {
        id = android.R.id.content
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    } }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(12.dp)),
        update = { view ->
            view.setTileSource(TileSourceFactory.MAPNIK)
            view.controller.setZoom(zoomLevel)
            view.controller.setCenter(initialPosition)
        }
    )

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MobileclientTheme {
        val statusText = remember {
            mutableStateOf("Status")
        }
        val latitude = remember {
            mutableStateOf("100.0")
        }
        MainScreen(
            statusText = statusText,
            latitude = latitude,
            longitude = latitude,
            speed = latitude,
            startPublish = {},
            navigateToAuth = {}
        )
    }
}