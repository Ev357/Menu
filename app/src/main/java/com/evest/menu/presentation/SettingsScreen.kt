package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.webkit.URLUtil
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.NoAccounts
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
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.evest.menu.R
import kotlinx.coroutines.launch

@Composable
fun SettingsItem(
    preference: Preference,
    content: @Composable (SharedPreferences, Preference) -> Unit
) {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences(preference.name, Context.MODE_PRIVATE)
    }
    Column {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(stringResource(preference.label), fontSize = 20.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            content(preferences, preference)
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class)
@SuppressLint("CommitPrefEdits")
@Composable
fun SettingsScreen(navController: NavHostController) {
    val listState = rememberScalingLazyListState(0)
    val context = LocalContext.current
    val dao = MenuDatabase.getInstance(context).dao
    @Suppress("UNCHECKED_CAST") val viewModel = viewModel<MenuViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MenuViewModel(dao, context.applicationContext as Application) as T
            }
        }
    )

    val cryptoManager = CryptoManager()
    val hasInternet = isInternetAvailable(context)

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
                    SettingsItem(Preference.AccountPreference) { preferences, preference ->
                        val isLoggedOption = preference.options.first()
                        val usernameOption = Preference.AccountPreference.options[1]
                        val passwordOption = Preference.AccountPreference.options[2]
                        val usernameSegmentOption = Preference.AccountPreference.options[3]

                        var isLogged by mutableStateOf(
                            preferences.getBoolean(
                                isLoggedOption.name,
                                isLoggedOption.defaultValue as Boolean
                            )
                        )

                        Chip(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(40.dp),
                            enabled = isLogged || hasInternet,
                            onClick = {
                                if (isLogged) {
                                    preferences.edit().apply {
                                        putString(
                                            usernameOption.name,
                                            ""
                                        )
                                        putString(
                                            passwordOption.name,
                                            ""
                                        )
                                        putString(
                                            usernameSegmentOption.name,
                                            ""
                                        )
                                        putBoolean(
                                            isLoggedOption.name,
                                            false
                                        )
                                    }.apply().also {
                                        isLogged = false
                                    }
                                } else {
                                    navController.navigate(Screen.LoginScreen.route)
                                }
                            },
                            label = {
                                if (isLogged) {
                                    val encryptedUsernameSubstring = preferences.getString(
                                        usernameSegmentOption.name,
                                        usernameSegmentOption.defaultValue as String
                                    ) ?: return@Chip

                                    val usernameSubstring =
                                        cryptoManager.decrypt(encryptedUsernameSubstring)
                                            .split("").filter { it.isNotEmpty() }
                                            .joinToString("...")

                                    Column {
                                        Text(
                                            "${stringResource(R.string.logged_as)}: $usernameSubstring",
                                            modifier = Modifier.fillMaxWidth(),
                                            fontSize = 10.sp
                                        )
                                        Text(stringResource(R.string.logout), fontSize = 14.sp)
                                    }
                                } else {
                                    Text(stringResource(R.string.login))
                                }
                            },
                            icon = {
                                if (isLogged) {
                                    Icon(Icons.Default.NoAccounts, stringResource(R.string.logout))
                                } else {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        stringResource(R.string.login)
                                    )
                                }
                            },
                            colors = ChipDefaults.chipColors(
                                MaterialTheme.colors.secondary
                            )
                        )
                    }
                }
                item {
                    SettingsItem(Preference.MenuPreference) { preferences, preference ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            preference.options.forEach { option ->
                                var isChecked by remember {
                                    mutableStateOf(
                                        preferences.getBoolean(
                                            option.name,
                                            option.defaultValue as Boolean
                                        )
                                    )
                                }

                                ToggleChip(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(40.dp),
                                    checked = isChecked,
                                    onCheckedChange = { isNewChecked ->
                                        preferences.edit().apply {
                                            putBoolean(option.name, isNewChecked)
                                        }.apply()
                                        isChecked = isNewChecked
                                    },
                                    label = {
                                        Text(stringResource(option.label))
                                    },
                                    toggleControl = {
                                        Checkbox(isChecked)
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    SettingsItem(Preference.ServerPreference) { preferences, preference ->
                        val option = preference.options.first()
                        var serverAddress by remember {
                            mutableStateOf(
                                preferences.getString(
                                    option.name,
                                    option.defaultValue as String
                                ).orEmpty()
                            )
                        }
                        BasicTextField(
                            value = serverAddress,
                            onValueChange = { newText ->
                                val trimmedNewText = newText.trim()
                                preferences.edit().apply {
                                    putString(option.name, trimmedNewText)
                                }.apply()
                                serverAddress = trimmedNewText
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .border(
                                        width = 2.dp,
                                        color = if (URLUtil.isValidUrl(serverAddress)) {
                                            Color.White
                                        } else {
                                            Color.Red
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    serverAddress.ifEmpty {
                                        "https://..."
                                    },
                                    color = if (serverAddress.isEmpty()) {
                                        Color.Gray
                                    } else {
                                        Color.White
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Column {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.other), fontSize = 20.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Chip(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(40.dp),
                                onClick = {
                                    viewModel.onEvent(
                                        MenuEvent.OpenAlert(
                                            "clear_database",
                                            context.resources.getString(R.string.clear_database),
                                            context.resources.getString(R.string.are_you_sure)
                                        )
                                    )
                                },
                                label = {
                                    Text(stringResource(R.string.clear_database))
                                },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = MaterialTheme.colors.error
                                ),
                                icon = {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        stringResource(R.string.clear_database)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
