package com.evest.menu.presentation

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import com.evest.menu.R
import com.evest.menu.presentation.ui.theme.MenuTheme

class AlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionName = intent.getStringExtra("ACTION_NAME")
        val titleText = intent.getStringExtra("TITLE_TEXT")
        val message = intent.getStringExtra("MESSAGE")
        if (actionName.isNullOrEmpty() || titleText.isNullOrEmpty() || message.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            MenuTheme {
                AlertComposable(actionName, this, titleText, message)
            }
        }
    }
}

@Composable
fun AlertComposable(
    actionType: String,
    alertActivity: AlertActivity,
    titleText: String,
    message: String
) {
    val dao = MenuDatabase.getInstance(alertActivity).dao
    @Suppress("UNCHECKED_CAST") val viewModel = viewModel<MenuViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MenuViewModel(dao, alertActivity.applicationContext as Application) as T
            }
        }
    )


    Alert(
        title = { Text(titleText, textAlign = TextAlign.Center) },
        negativeButton = {
            Button(
                colors = ButtonDefaults.secondaryButtonColors(),
                onClick = {
                    alertActivity.finish()
                }
            ) {
                Icon(Icons.Default.Close, stringResource(R.string.cancel))
            }
        },
        positiveButton = {
            Button(
                onClick = {
                    executeAction(actionType, viewModel::onEvent, alertActivity)
                }
            ) {
                Icon(Icons.Default.Check, stringResource(R.string.confirm))
            }
        },
        contentPadding =
        PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )
    }
}

private fun executeAction(
    actionName: String,
    onEvent: (MenuEvent) -> Unit,
    alertActivity: AlertActivity
) {
    when (actionName) {
        "clear_database" -> {
            onEvent(MenuEvent.ClearDatabase)
        }
    }
    alertActivity.finish()
}