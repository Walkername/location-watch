package ru.locationwatch.mobile_client

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import ru.locationwatch.mobile_client.ui.theme.MobileclientTheme
import java.util.Properties

class MainActivity : ComponentActivity() {

    private lateinit var appConfig: AppConfig

    private var serverURI = ""
    private var userId = ""
    private var userPassword = ""
    private var mqttTopic = "events"

    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appConfig = AppConfig(applicationContext)

        serverURI = appConfig.serverURI
        userId = appConfig.userId
        userPassword = appConfig.userPassword

        enableEdgeToEdge()
        setContent {
            MobileclientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val statusText = remember {
                        mutableStateOf("Status")
                    }
                    MainScreen(
                        statusText = statusText,
                        startPublish = { startPublish(statusText) }
                    )
                }
            }
        }
    }

    fun startPublish(
        statusText: MutableState<String>
    )  {
        val clientId = MqttClient.generateClientId()
        val client = MqttAndroidClient(this.applicationContext, serverURI, clientId)
        val options = MqttConnectOptions()
        try {
            options.userName = userId
            options.password = userPassword.toCharArray()
            client.connect(options, null, object: IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    mHandler.post {
                        statusText.value += ": Connected"
                    }

                    publish(client)

                    val mRunnableTask = object: Runnable {
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
        val message = "Hello";
        try {
            client.publish(publishTopic, MqttMessage(message.toByteArray()))
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}

@Composable
fun MainScreen(
    statusText: MutableState<String>,
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
                Text("One")
                Text("Two")
                Text("Three")
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

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MobileclientTheme {
        val statusText = remember {
            mutableStateOf("Status")
        }
        MainScreen(
            statusText = statusText,
            startPublish = {}
        )
    }
}