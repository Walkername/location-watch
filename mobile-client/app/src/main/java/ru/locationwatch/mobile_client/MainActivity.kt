package ru.locationwatch.mobile_client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
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
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import ru.locationwatch.mobile_client.config.AppConfig
import ru.locationwatch.mobile_client.ui.theme.MobileclientTheme
import ru.locationwatch.mobile_client.ui.screens.AuthorizationScreen
import ru.locationwatch.mobile_client.ui.screens.MainScreen

class MainActivity : ComponentActivity() {

    private lateinit var appConfig: AppConfig

    private val PERMISSIONS_FINE_LOCATIONS = 99

    private var serverURI = ""
    private var userId = ""
    private var userPassword = ""
    private var mqttTopic = "events"

    private val mHandler = Handler(Looper.getMainLooper())

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback : LocationCallback
    private var requestingLocationUpdates = true

    private val latitude = mutableStateOf("")
    private val longitude = mutableStateOf("")
    private val speed = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appConfig = AppConfig(applicationContext)

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
                    latitude.value = location.latitude.toString()
                    longitude.value = location.longitude.toString()
                    speed.value = location.speed.toString()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
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

                    NavHost(
                        navController = navController,
                        startDestination = AuthorizationScreen
                    ) {
                        composable<MainScreen> {
                            MainScreen(
                                statusText = statusText,
                                latitude = latitude,
                                longitude = longitude,
                                speed = speed,
                                startPublish = { startPublish(statusText) },
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
        fusedLocationProviderClient?.let {
            it.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateGPS() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener {
                latitude.value = it.latitude.toString()
                longitude.value = it.longitude.toString()
                speed.value = it.speed.toString()
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

                    val mRunnableTask = object : Runnable {
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

    fun publish(client: MqttAndroidClient) {
        val publishTopic = "\$devices/$userId/$mqttTopic"
        val message = "latitude: ${latitude.value}, longitude: ${longitude.value}, speed: ${speed.value}"
        try {
            client.publish(publishTopic, MqttMessage(message.toByteArray()))
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
