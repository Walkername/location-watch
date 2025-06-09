package ru.locationwatch.mobile_client.ui.screens

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import ru.locationwatch.mobile_client.R
import ru.locationwatch.mobile_client.network.models.ZoneResponse
import ru.locationwatch.mobile_client.ui.AuthViewModel
import ru.locationwatch.mobile_client.ui.NotificationViewModel
import ru.locationwatch.mobile_client.ui.TokenUiState
import ru.locationwatch.mobile_client.ui.UserUiState
import ru.locationwatch.mobile_client.ui.UserViewModel
import ru.locationwatch.mobile_client.ui.ZoneUiState
import ru.locationwatch.mobile_client.ui.ZoneViewModel
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    notificationData: MutableState<Pair<String?, String?>?>,
    onNotificationShown: () -> Unit,
    statusText: MutableState<String>,
    latitude: MutableState<Double?>,
    longitude: MutableState<Double?>,
    speed: MutableState<Double?>,
    tripStatus: MutableState<Boolean>,
    startPublish: () -> Unit,
    stopPublish: () -> Unit,
    navigateToAuth: () -> Unit
) {
    NotificationPermissionHandler()

    // Show notification dialog if data exists
    notificationData.value?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = onNotificationShown,
            title = { Text(title ?: "Notification") },
            text = { Text(body ?: "No message content") },
            confirmButton = {
                Button(onClick = onNotificationShown) {
                    Text("OK")
                }
            }
        )
    }

    var selectedZone by remember {
        mutableStateOf<ZoneResponse?>(null)
    }

    val app = LocalContext.current.applicationContext as Application
    val userViewModelFactory = UserViewModel.createFactory(app)
    val userViewModel: UserViewModel = viewModel(factory = userViewModelFactory)

    val authViewModelFactory = AuthViewModel.createFactory(app)
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

    val notificationViewModelFactory = NotificationViewModel.createFactory(app)
    val notificationViewModel: NotificationViewModel =
        viewModel(factory = notificationViewModelFactory)

    val zoneViewModelFactory = ZoneViewModel.createFactory(app)
    val zoneViewModel: ZoneViewModel = viewModel(factory = zoneViewModelFactory)

    val zoneUiState = zoneViewModel.zoneUiState

    val zones = remember {
        mutableStateOf<List<ZoneResponse>>(emptyList())
    }

    LaunchedEffect(Unit) {
        authViewModel.loadUserId()
        if (authViewModel.userId != null) {
            userViewModel.fetchUser(authViewModel.userId!!)
        } else {
            navigateToAuth()
        }

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
        0.65f to Color.White,
        0.8f to Color(0xFF7EE882),
        1f to Color(0xFF1EE1AE)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = colorStops
                )
            )
    ) {
        NavigationBar(
            modifier = Modifier,
            navigateToAuth = { navigateToAuth() },
            authViewModel = authViewModel,
            notificationViewModel = notificationViewModel,
            username = username
        )
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                MapContainer(
                    modifier = Modifier
                        .weight(8f),
                    zones = zones.value,
                    latitude = latitude,
                    longitude = longitude,
                    onZoneSelected = { zone -> selectedZone = zone }
                )

                StatusBar(
                    modifier = Modifier
                        .weight(0.5f),
                    statusText = statusText
                )

                MenuBar(
                    modifier = Modifier
                        .weight(1f),
                    tripStatus = tripStatus,
                    startPublish = { startPublish() },
                    stopPublish = { stopPublish() }
                )
            }

            ZoneBottomSheet(
                selectedZone = selectedZone,
                onDismiss = { selectedZone = null }
            )
        }
    }
}

@Composable
fun NotificationPermissionHandler() {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
        } else {
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (status != PackageManager.PERMISSION_GRANTED) {
                delay(300)
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun OpenStreetMap(
    modifier: Modifier = Modifier,
    initialPosition: GeoPoint = GeoPoint(59.937500, 30.308611),
    zoomLevel: Double = 14.0,
    zones: List<ZoneResponse> = emptyList(),
    // latitude and longitude are assigned manually to check it in virtual device
    // but for production you need to remove these assignments
    // and pass them by your gps location (in MainScreen pass arguments)
    latitude: MutableState<Double?> = mutableStateOf(59.937500),
    longitude: MutableState<Double?> = mutableStateOf(30.308611),
    onZoneSelected: (ZoneResponse) -> Unit
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
            icon = BitmapDrawable(
                context.resources, Bitmap.createScaledBitmap(
                    ContextCompat.getDrawable(context, R.drawable.gps_loc_1)!!.toBitmap(),
                    40, 40, true
                )
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
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
        latitude.value?.let { lat ->
            longitude.value?.let { lon ->
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
        zones.reversed().forEach { zone ->
            zone.area?.let { cords ->
                val poly = Polygon().apply {
                    points = cords.map { GeoPoint(it.latitude!!, it.longitude!!) }
                    fillPaint.apply {
                        color = when (zone.typeName) {
                            "NO_SPEED" -> 0x66FF0000
                            "LESS_SPEED" -> 0x660048FF
                            else -> Color.Transparent.toArgb()
                        }
                        style = Paint.Style.FILL
                    }
                    outlinePaint.apply {
                        color = Color.Black.toArgb()
                        strokeWidth = 2f
                        style = Paint.Style.STROKE
                    }
                    setOnClickListener { _, _, _ ->
                        onZoneSelected(zone)
                        true
                    }
                }
                mapView.overlays.add(poly)
            }
        }
        mapView.overlays.remove(userMarker)
        mapView.overlays.add(userMarker)
        mapView.postInvalidate()
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
    )

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }
}

@Composable
fun ZoneBottomSheet(
    selectedZone: ZoneResponse?,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { screenHeightDp.toPx() }
    val sheetHeightPx = screenHeightPx * 0.25f

    val baseOffset = remember { Animatable(sheetHeightPx) }
    var dragOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedZone) {
        if (selectedZone != null) {
            baseOffset.animateTo(0f, animationSpec = tween(300))
        } else {
            baseOffset.animateTo(sheetHeightPx, animationSpec = tween(300))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedZone != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000))
                    .clickable {
                        scope.launch {
                            baseOffset.animateTo(sheetHeightPx, tween(300))
                            onDismiss()
                        }
                    }
            )
        }

        selectedZone?.let { zone ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(screenHeightDp * 0.25f)
                    .offset { IntOffset(0, (baseOffset.value + dragOffset).roundToInt()) }
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { },
                            onVerticalDrag = { change, dragAmount ->
                                val newDragOffset = dragOffset + dragAmount
                                val newTotalOffset = baseOffset.value + newDragOffset

                                dragOffset = when {
                                    newTotalOffset < 0 -> -baseOffset.value
                                    newTotalOffset > sheetHeightPx -> sheetHeightPx - baseOffset.value
                                    else -> newDragOffset
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val totalOffset = baseOffset.value + dragOffset
                                    val threshold = sheetHeightPx * 0.5f

                                    // Snap to current position first
                                    baseOffset.snapTo(totalOffset)
                                    dragOffset = 0f

                                    if (totalOffset > threshold) {
                                        baseOffset.animateTo(sheetHeightPx, tween(300))
                                        onDismiss()
                                    } else {
                                        baseOffset.animateTo(0f, tween(300))
                                    }
                                }
                            }
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(RoundedCornerShape(100))
                        .background(
                            MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.4f)
                        )
                )
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Zone Information",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        IconButton(
                            onClick = {
                                scope.launch {
                                    baseOffset.animateTo(sheetHeightPx, tween(300))
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text("Title: ${zone.title}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))

                    val typeName: String
                    val info: String
                    if (zone.typeName == "NO_SPEED") {
                        typeName = "Restricted Area"
                        info = "You cannot be in this area."
                    } else {
                        typeName = "Speed Limited Area"
                        info = "You may be in this area, but you must obey the speed limit."
                    }

                    Text("Type: $typeName")
                    Spacer(Modifier.height(4.dp))
                    Text("Max. Speed: ${zone.speed} km/h")
                    Spacer(Modifier.height(4.dp))
                    Text(info)
                }
            }
        }
    }
}

@Composable
fun MapContainer(
    modifier: Modifier,
    zones: List<ZoneResponse>,
    latitude: MutableState<Double?>,
    longitude: MutableState<Double?>,
    onZoneSelected: (ZoneResponse) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        OpenStreetMap(
            zones = zones,
            initialPosition = GeoPoint(59.937500, 30.308611),
            // For development during checking on virtual device use your own location gps
            // You can do it in OpenStreetMap
//            latitude = latitude,
//            longitude = longitude,
            onZoneSelected = onZoneSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBar(
    modifier: Modifier,
    navigateToAuth: () -> Unit,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    username: MutableState<String>
) {
    when (val tokenUiState = notificationViewModel.tokenUiState) {
        is TokenUiState.Loading -> {
        }

        is TokenUiState.Success -> {
            LaunchedEffect(tokenUiState) {
                notificationViewModel.markTokenStatus(false)
                authViewModel.resetTokens()
                navigateToAuth()
            }
        }

        is TokenUiState.Error -> {
            return
        }
    }

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = username.value,
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        },
        actions = {
            IconButton({
                // Send request to backend to Delete firebase token in DB
                val firebaseToken = notificationViewModel.getTokenAndStatus().first
                firebaseToken?.let {
                    notificationViewModel.deleteFirebaseToken(it)
                }
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Exit"
                )
            }
        }
    )
}

@Composable
fun MenuBar(
    modifier: Modifier,
    tripStatus: MutableState<Boolean>,
    startPublish: () -> Unit,
    stopPublish: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StartTripButton(
            isActive = tripStatus.value,
            onClick = {
                if (tripStatus.value) {
                    stopPublish()
                    tripStatus.value = false
                } else {
                    startPublish()
                    tripStatus.value = true
                }
            }
        )
    }
}

@Composable
fun StatusBar(
    modifier: Modifier,
    statusText: MutableState<String>
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statusText.value
        )
    }
}

@Composable
fun StartTripButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = Color(0x40000000),
                spotColor = Color(0x40000000)
            )
            .border(
                border = BorderStroke(1.5.dp, Color.LightGray),
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(Color.White)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.LocationOn else Icons.Filled.PlayArrow,
                    contentDescription = if (isActive) "Trip in progress" else "Start trip",
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF333333)
                )

                // Active state indicator
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
}