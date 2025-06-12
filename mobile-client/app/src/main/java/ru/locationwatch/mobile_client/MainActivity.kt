package ru.locationwatch.mobile_client

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import ru.locationwatch.mobile_client.config.AppConfig
import ru.locationwatch.mobile_client.network.NotificationManager
import ru.locationwatch.mobile_client.network.TokenManager
import ru.locationwatch.mobile_client.network.models.GPSDataRequest
import ru.locationwatch.mobile_client.ui.AuthViewModel
import ru.locationwatch.mobile_client.ui.RefreshUiState
import ru.locationwatch.mobile_client.ui.theme.MobileclientTheme
import ru.locationwatch.mobile_client.ui.screens.AuthorizationScreen
import ru.locationwatch.mobile_client.ui.screens.MainScreen
import java.util.Date

class MainActivity : ComponentActivity() {

    private lateinit var appConfig: AppConfig

    private val PERMISSIONS_FINE_LOCATIONS = 99

    private var serverURI = ""
    private var userId = ""
    private var userPassword = ""
    private var mqttTopic = "events"

    private val mHandler = Handler(Looper.getMainLooper())

    private lateinit var mRunnableTask: Runnable

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = true

    private val latitude = mutableStateOf<Double?>(null)
    private val longitude = mutableStateOf<Double?>(null)
    private val speed = mutableStateOf<Double?>(null)

    private val tripStatus = mutableStateOf(false)

    private val notificationData = mutableStateOf<Pair<String?, String?>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appConfig = AppConfig(applicationContext)

        handleIntent(intent)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        serverURI = appConfig.serverURI
        userId = appConfig.userId
        userPassword = appConfig.userPassword

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30 * 1000
        ).setMinUpdateIntervalMillis(5 * 1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    latitude.value = location.latitude
                    longitude.value = location.longitude
                    speed.value = location.speed.toDouble()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            // Collect notifications from the shared flow
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    NotificationManager.notificationFlow.collect { data ->
                        notificationData.value = data
                    }
                }
            }

            MobileclientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val statusText = remember {
                        mutableStateOf("Status")
                    }

                    updateGPS()

                    val app = LocalContext.current.applicationContext as Application
                    val viewModelFactory = AuthViewModel.createFactory(app)
                    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)

                    val accessToken = viewModel.getAccessToken()
                    val accessExpirationTime = viewModel.getAccessExpirationTime()
                    val refreshToken = viewModel.getRefreshToken()
                    val refreshExpirationTime = viewModel.getRefreshExpirationTime()
                    val refreshUiState = viewModel.refreshUiState
                    val currentTime = Date().time
                    var authStatus = false

                        if (accessToken != null) {
                            accessExpirationTime?.let { accessExpTime ->
                                if (accessExpTime * 1000 > currentTime) {
                                    authStatus = true
                                } else {
                                    Log.e("token", "token is expired")
                                    authStatus = false

                                    // If refreshToken is also expired, then authStatus = false
                                    if (refreshToken != null) {
                                        refreshExpirationTime?.let { refreshExpTime ->
                                            if (refreshExpTime * 1000 > currentTime) {
                                                Log.e("refresh-token", "refresh token is valid")
                                                // send request to refresh tokens
                                                LaunchedEffect(Unit) {
                                                    viewModel.refreshTokens(refreshToken)
                                                }
                                            } else { // refresh token is expired
                                                authStatus = false
                                                viewModel.resetTokens()
                                                Log.e("refresh-token", "refresh token is expired")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    when (refreshUiState) {
                        is RefreshUiState.Loading -> {
                        }
                        is RefreshUiState.Success -> {
                            Log.e("refresh tokens", "tokens were updated")
                            authStatus = true
                        }
                        is RefreshUiState.Error -> {
                            authStatus = false
                        }
                    }
                    Log.e("auth-status", "status: $authStatus")

                    NavHost(
                        navController = navController,
                        startDestination = if (authStatus) MainScreen else AuthorizationScreen
                    ) {
                        composable<MainScreen> {
                            MainScreen(
                                notificationData = notificationData,
                                onNotificationShown = { notificationData.value = null },
                                statusText = statusText,
                                latitude = latitude,
                                longitude = longitude,
                                speed = speed,
                                tripStatus = tripStatus,
                                startPublish = { startPublish(statusText) },
                                stopPublish = { stopPublish(statusText) },
                                navigateToAuth = { navController.navigate(AuthorizationScreen) }
                            )
                        }
                        composable<AuthorizationScreen> {
                            AuthorizationScreen(
                                navigateToMain = { navController.navigate(MainScreen) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent) // Handle new intent if app was already open
    }

    private fun handleIntent(intent: Intent) {
        // Check if launched from notification
        if (intent.action == "OPEN_DETAILS_ACTION" || intent.data?.scheme == "myapp") {
            notificationData.value = Pair(
                intent.getStringExtra("ntf_title"),
                intent.getStringExtra("ntf_body")
            )

            // Clear intent to prevent duplicate handling
            this.intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        when (requestCode) {
            PERMISSIONS_FINE_LOCATIONS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //updateGPS()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Permissions are required",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        fusedLocationProviderClient?.let { client ->
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }

    private fun updateGPS() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener {
                latitude.value = it.latitude
                longitude.value = it.longitude
                speed.value = it.speed.toDouble()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_FINE_LOCATIONS
                )
            }
        }
    }

    private fun startPublish(
        statusText: MutableState<String>
    ) {
        val clientId = MqttClient.generateClientId()
        val client = MqttAndroidClient(this.applicationContext, serverURI, clientId)
        val options = MqttConnectOptions()
        try {
            options.userName = userId
            options.password = userPassword.toCharArray()
            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    mHandler.post {
                        statusText.value = "Status: Connected"
                    }

                    publish(client)

                    mRunnableTask = object : Runnable {
                        override fun run() {
                            publish(client)
                            mHandler.postDelayed(this, 5000)
                        }
                    }

                    mHandler.postDelayed(mRunnableTask, 5000)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    mHandler.post {
                        statusText.value += ": Connected failed"
                    }
                }

            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun stopPublish(statusText: MutableState<String>) {
        mHandler.removeCallbacks(mRunnableTask)
        statusText.value = "Status: Disconnected"
    }

    private fun publish(client: MqttAndroidClient) {
        // if one of gps data is null
        // then data will not be send to backend

        val tokenManager = TokenManager(this)
        val jwtUserId = tokenManager.getUserId()

        if (
            latitude.value == null
            || longitude.value == null
            || speed.value == null
        ) {
            return
        }

        val publishTopic = "\$devices/$userId/$mqttTopic"

        val gpsData = GPSDataRequest(
            jwtUserId,
            latitude.value,
            longitude.value,
            speed.value
        )
        val jsonString = Json.encodeToString(gpsData)

        try {
            client.publish(publishTopic, MqttMessage(jsonString.toByteArray()))
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
