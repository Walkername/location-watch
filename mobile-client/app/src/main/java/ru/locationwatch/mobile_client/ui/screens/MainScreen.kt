package ru.locationwatch.mobile_client.ui.screens

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import ru.locationwatch.mobile_client.R
import ru.locationwatch.mobile_client.network.models.ZoneResponse
import ru.locationwatch.mobile_client.ui.AuthViewModel
import ru.locationwatch.mobile_client.ui.UserUiState
import ru.locationwatch.mobile_client.ui.UserViewModel
import ru.locationwatch.mobile_client.ui.ZoneUiState
import ru.locationwatch.mobile_client.ui.ZoneViewModel
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

    val zoneViewModelFactory = ZoneViewModel.createFactory(app)
    val zoneViewModel: ZoneViewModel = viewModel(factory = zoneViewModelFactory)

    val zoneUiState = zoneViewModel.zoneUiState

    val zones = remember {
        mutableStateOf<List<ZoneResponse>>(emptyList())
    }

    LaunchedEffect(Unit) {
        authViewModel.loadUserId()
        userViewModel.fetchUser(authViewModel.userId!!)

        zoneViewModel.fetchZones()
    }

    val userUiState = userViewModel.userUiState

    val username = remember {
        mutableStateOf("")
    }

    when (userUiState) {
        is UserUiState.Loading -> {
        }

        is UserUiState.Success -> {
            val userName: String =
                if (userUiState.user.username != null) userUiState.user.username!! else ""
            username.value = userName
        }

        is UserUiState.Error -> {
            if (userUiState.message == "Invalid JWT token") {
                LaunchedEffect(Unit) {
                    authViewModel.resetTokens()
                    navigateToAuth()
                }
            }
        }
    }

    when (zoneUiState) {
        is ZoneUiState.Loading -> {
        }

        is ZoneUiState.Success -> {
            zones.value = zoneUiState.zones
        }

        is ZoneUiState.Error -> {
            Log.e("zones", zoneUiState.message)
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
            onClick = {
                navigateToAuth()
                authViewModel.resetTokens()
            }
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
                        initialPosition = GeoPoint(59.937500, 30.308611),
                        zones = zones.value
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
                    .size(80.dp),
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
    zoomLevel: Double = 12.0,
    zones: List<ZoneResponse> = emptyList(),
    latitude: MutableState<String> = mutableStateOf("59.937500"),
    longitude: MutableState<String> = mutableStateOf("30.308611")
) {
    val context = LocalContext.current

    val mapView = remember {
        MapView(context).apply {
            id = android.R.id.content
            layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT, MATCH_PARENT
            )
            Configuration.getInstance().userAgentValue = context.packageName
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(zoomLevel)
            controller.setCenter(initialPosition)
        }
    }

    val userMarker = remember {
        Marker(mapView).apply {
            icon = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(
                ContextCompat.getDrawable(context, R.drawable.gps_loc)!!.toBitmap(),
                40, 40, true
            ))
            setOnMarkerClickListener { _, _ -> true }
            mapView.overlays.add(this)
        }
    }

    // 3) Pulsing brightness side‑effect (only invalidates the view)
    val infiniteTransition = rememberInfiniteTransition()
    val brightness by infiniteTransition.animateFloat(
        1f, 1.5f,
        infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse)
    )
    LaunchedEffect(brightness) {
        userMarker.icon?.colorFilter = ColorMatrixColorFilter(
            ColorMatrix().apply { setScale(brightness, brightness, brightness, 1f) }
        )
        mapView.postInvalidate()  // only redraws, doesn’t re‑run overlays logic
    }

    // 4) Update the user‑marker position only when lat/long really change
    LaunchedEffect(latitude.value, longitude.value) {
        latitude.value.toDoubleOrNull()?.let { lat ->
            longitude.value.toDoubleOrNull()?.let { lon ->
                val p = GeoPoint(lat, lon)
                userMarker.position = p
                mapView.controller.animateTo(p)
            }
        }
    }

    LaunchedEffect(zones) {
        // remove any old zone polygons
        mapView.overlays
            .filterIsInstance<Polygon>()
            .forEach { mapView.overlays.remove(it) }

        // add new ones
        zones.forEach { zone ->
            zone.area?.let { cords ->
                val poly = Polygon().apply {
                    points = cords.map { GeoPoint(it.x!!, it.y!!) }
                    fillPaint.apply {
                        color = when (zone.typeName) {
                            "NO_SPEED"   -> 0x80FF0000.toInt()
                            "LESS_SPEED" -> 0x800048FF.toInt()
                            else         -> Color.Transparent.toArgb()
                        }
                        style = Paint.Style.FILL
                    }
                    outlinePaint.apply {
                        color = Color.Black.toArgb()
                        strokeWidth = 2f
                        style = Paint.Style.STROKE
                    }
                }
                mapView.overlays.add(poly)
            }
        }
        mapView.postInvalidate()
    }

    AndroidView(
        factory  = { mapView },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
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