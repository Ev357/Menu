package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import androidx.wear.activity.ConfirmationActivity
import com.evest.menu.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        ContextCompat.getSystemService(context, ConnectivityManager::class.java)
    val currentNetwork = connectivityManager?.activeNetwork
    val networkCapabilities = connectivityManager?.getNetworkCapabilities(currentNetwork)

    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun capitalize(text: String): String {
    return text.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }
}

fun formatText(text: String): String {
    return capitalize(text.trim()).replace(",(?!\\s)".toRegex(), ", ")
        .replace("  ", " ")
        .run { if (endsWith('.')) this else "$this." }
}

fun getMealTypeLabel(string: String): Int? {
    return when (string) {
        "breakfast" -> R.string.breakfast
        "soup" -> R.string.soup
        "lunch1" -> R.string.lunch1
        "lunch2" -> R.string.lunch2
        "dinner" -> R.string.dinner
        else -> null
    }
}

@SuppressLint("WearRecents")
fun startConfirmation(context: Context, message: String = "", type: String = "success") {
    val intent =
        Intent(context, ConfirmationActivity::class.java).apply {
            putExtra(
                ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                if (type == "success") {
                    ConfirmationActivity.SUCCESS_ANIMATION
                } else {
                    ConfirmationActivity.FAILURE_ANIMATION
                }
            )
            if (message.isNotEmpty()) {
                putExtra(
                    ConfirmationActivity.EXTRA_MESSAGE,
                    message
                )
            }
        }
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun getDayMonth(date: LocalDate): String {
    val day = DateTimeFormatter.ofPattern("d").format(date)
    val month = capitalize(DateTimeFormatter.ofPattern("MMM").format(date))

    return "$day $month"
}