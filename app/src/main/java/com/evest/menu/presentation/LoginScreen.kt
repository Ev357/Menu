package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.evest.menu.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("CommitPrefEdits")
@OptIn(ExperimentalWearFoundationApi::class, DelicateCoroutinesApi::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val listState = rememberScalingLazyListState(0)

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        },
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val focusRequester = rememberActiveFocusRequester()
            val coroutineScope = rememberCoroutineScope()

            val context = LocalContext.current
            val dao = MenuDatabase.getInstance(context).dao
            @Suppress("UNCHECKED_CAST") val viewModel = viewModel<MenuViewModel>(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MenuViewModel(dao, context.applicationContext as Application) as T
                    }
                }
            )

            val preferences = remember {
                context.getSharedPreferences(
                    Preference.AccountPreference.name,
                    Context.MODE_PRIVATE
                )
            }

            val scope = rememberCoroutineScope()
            var isLoading by remember {
                mutableStateOf(false)
            }

            val isLoggedOption = Preference.AccountPreference.options.first()
            val usernameOption = Preference.AccountPreference.options[1]
            val passwordOption = Preference.AccountPreference.options[2]
            val usernameSegmentOption = Preference.AccountPreference.options[3]

            var username by remember {
                mutableStateOf("")
            }
            var password by remember {
                mutableStateOf("")
            }

            val cryptoManager = CryptoManager()

            ScalingLazyColumn(
                Modifier
                    .onRotaryScrollEvent {
                        coroutineScope.launch {
                            listState.scrollBy(it.verticalScrollPixels)
                            listState.animateScrollBy(0f)
                        }
                        true
                    }
                    .focusRequester(focusRequester)
                    .focusable(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        value = username,
                        onValueChange = { username = it.trim() },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.White
                        ),
                        decorationBox = { innerTextField ->
                            if (username.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.username),
                                    fontSize = 16.sp,
                                    color = Color.LightGray
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                item {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        value = password,
                        onValueChange = { password = it },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.White
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        decorationBox = { innerTextField ->
                            if (password.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.password),
                                    fontSize = 16.sp,
                                    color = Color.LightGray
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .height(40.dp),
                        onClick = {
                            scope.launch {
                                var loggedSuccessfully = false
                                val job = GlobalScope.launch {
                                    isLoading = true

                                    val isLoginValid = try {
                                        login(context, username, password)
                                        true
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                        false
                                    }

                                    if (isLoginValid) {
                                        preferences.edit().apply {
                                            putString(
                                                usernameOption.name,
                                                cryptoManager.encrypt(username)
                                            )
                                            putString(
                                                passwordOption.name,
                                                cryptoManager.encrypt(password)
                                            )
                                            putString(
                                                usernameSegmentOption.name,
                                                cryptoManager.encrypt("${username.first()}${username.last()}")
                                            )
                                            putBoolean(
                                                isLoggedOption.name,
                                                true
                                            )
                                        }.apply()
                                        viewModel.onEvent(MenuEvent.FetchMenu())
                                        loggedSuccessfully = true
                                    } else {
                                        startConfirmation(
                                            context,
                                            context.resources.getString(R.string.login_failed),
                                            "error"
                                        )
                                    }
                                    isLoading = false
                                }
                                job.join()
                                if (loggedSuccessfully) {
                                    navController.popBackStack()
                                    scope.launch {
                                        startConfirmation(
                                            context,
                                            context.resources.getString(R.string.login_successful)
                                        )
                                    }
                                }
                            }
                        },
                    ) {
                        Text(stringResource(R.string.login))
                    }
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    indicatorColor = MaterialTheme.colors.secondary,
                    trackColor = MaterialTheme.colors.onBackground.copy(alpha = 0.1f),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}
