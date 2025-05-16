package ru.locationwatch.mobile_client.ui.screens

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.locationwatch.mobile_client.ui.LoginUiState
import ru.locationwatch.mobile_client.ui.AuthViewModel
import ru.locationwatch.mobile_client.ui.RegisterUiState
import ru.locationwatch.mobile_client.ui.theme.MobileclientTheme
import java.util.Date

@Composable
fun AuthorizationScreen(
    navigateToMain: () -> Unit
) {
    val colorStops = arrayOf(
        0.1f to Color.White,
        0.6f to Color(0xFF7EE882),
        1f to Color(0xFF1EE1AE)
    )
    val authType = remember {
        mutableStateOf(false)
    }

    val app = LocalContext.current.applicationContext as Application
    val viewModelFactory = AuthViewModel.createFactory(app)
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)

    val accessToken = viewModel.getAccessToken()
    val expirationTime = viewModel.getExpirationTime()
    val currentTime = Date().time

    if (accessToken != null) {
        expirationTime?.let {
            if (it * 1000 > currentTime) {
                LaunchedEffect(Unit) {
                    navigateToMain()
                }
            } else {
                Log.e("token", "token is expired")
                viewModel.resetTokens()
            }
        }
    }

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
                .fillMaxSize()
                .padding(50.dp, 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp)
        ) {
            Text(
                text = "Location Watch",
                fontSize = 24.sp
            )
            if (!authType.value) {
                SignInForm(
                    navigateToMain = { navigateToMain() },
                    viewModel = viewModel,
                    authType = authType
                )
            } else {
                SignUpForm(
                    authType = authType,
                    viewModel = viewModel
                )
            }


        }
    }
}

@Composable
fun SignUpForm(
    authType: MutableState<Boolean>,
    viewModel: AuthViewModel
) {
    val uiState: RegisterUiState = viewModel.registerUiState

    val username = remember {
        mutableStateOf("")
    }
    val password = remember {
        mutableStateOf("")
    }
    val passwordConfirmation = remember {
        mutableStateOf("")
    }
    val errorMessage = remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        AuthTextField(
            username,
            KeyboardOptions(keyboardType = KeyboardType.Text),
            "username"
        )
        AuthTextField(
            password,
            KeyboardOptions(keyboardType = KeyboardType.Password),
            "password"
        )
        AuthTextField(
            passwordConfirmation,
            KeyboardOptions(keyboardType = KeyboardType.Password),
            "password confirmation"
        )

        when (uiState) {
            is RegisterUiState.Loading -> {
            }

            is RegisterUiState.Success -> {

                AlertDialog(
                    onDismissRequest = { },
                    title = {
                        Text("Success")
                    },
                    text = {
                        Text("Registration successful!")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetRegisterState()
                                authType.value = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }

            is RegisterUiState.Error -> {
                errorMessage.value = uiState.message
            }
        }

        Text(
            text = errorMessage.value,
            color = Color(0xFFFF0000)
        )

        Button(
            onClick = {
                when {
                    username.value.length < 5 -> {
                        errorMessage.value = "Username should be greater than 5 characters"
                        return@Button
                    }
                    username.value.length > 20 -> {
                        errorMessage.value = "Username should be less than 20 characters"
                        return@Button
                    }
                    password.value.length < 5 -> {
                        errorMessage.value = "Password should be greater than 5 characters"
                        return@Button
                    }
                    password.value.length > 50 -> {
                        errorMessage.value = "Password should be less that 50 characters"
                        return@Button
                    }
                    password.value != passwordConfirmation.value -> {
                        errorMessage.value = "Password are not equal"
                        return@Button
                    }
                }

                viewModel.register(username.value, password.value)
            }
        ) {
            Text("Sign Up")
        }
        Text(
            text = "I have account",
            style = TextStyle(textDecoration = TextDecoration.Underline),
            color = Color(0xFF2196F3),
            modifier = Modifier.clickable { authType.value = false }
        )
    }
}

@Composable
fun SignInForm(
    navigateToMain: () -> Unit,
    viewModel: AuthViewModel,
    authType: MutableState<Boolean>
) {
    val uiState: LoginUiState = viewModel.loginUiState

    val username = remember {
        mutableStateOf("")
    }
    val password = remember {
        mutableStateOf("")
    }
    val errorMessage = remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        AuthTextField(
            username,
            KeyboardOptions(keyboardType = KeyboardType.Text),
            "username"
        )
        AuthTextField(
            password,
            KeyboardOptions(keyboardType = KeyboardType.Password),
            "password"
        )

        when (uiState) {
            is LoginUiState.Loading -> {
            }

            is LoginUiState.Success -> {
                LaunchedEffect(uiState) {
                    navigateToMain()
                    viewModel.resetLoginState()
                }
            }

            is LoginUiState.Error -> {
                errorMessage.value = uiState.message
            }
        }

        Text(
            text = errorMessage.value,
            color = Color(0xFFFF0000)
        )

        Button(
            onClick = {
                when {
                    username.value.length < 5 -> {
                        errorMessage.value = "Username should be greater than 5 characters"
                        return@Button
                    }
                    username.value.length > 20 -> {
                        errorMessage.value = "Username should be less than 20 characters"
                        return@Button
                    }
                    password.value.length < 5 -> {
                        errorMessage.value = "Password should be greater than 5 characters"
                        return@Button
                    }
                    password.value.length > 50 -> {
                        errorMessage.value = "Password should be less that 50 characters"
                        return@Button
                    }
                }
                viewModel.login(username.value, password.value)
            }
        ) {
            Text("Sign In")
        }
        Text(
            text = "Create account",
            style = TextStyle(textDecoration = TextDecoration.Underline),
            color = Color(0xFF2196F3),
            modifier = Modifier.clickable { authType.value = true }
        )
    }
}

@Composable
fun AuthTextField(
    state: MutableState<String>,
    keyboardOptions: KeyboardOptions,
    placeholder: String
) {
    var visualTransformation = VisualTransformation.None
    if (keyboardOptions.keyboardType == KeyboardType.Password) {
        visualTransformation = PasswordVisualTransformation()
    }
    OutlinedTextField(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0x9CF8F4E8),
            focusedContainerColor = Color(0x4DF8F4E8),
            focusedBorderColor = Color(0x00000000),
            unfocusedBorderColor = Color(0x00000000)
        ),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        value = state.value,
        onValueChange = { newString -> state.value = newString },
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0x80000000)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AuthorizationPreview() {
    MobileclientTheme {
        AuthorizationScreen(
            navigateToMain = { }
        )
    }
}