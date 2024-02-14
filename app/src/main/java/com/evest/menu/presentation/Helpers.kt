package com.evest.menu.presentation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import com.evest.menu.R
import java.util.Locale

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        ContextCompat.getSystemService(context, ConnectivityManager::class.java)
    val currentNetwork = connectivityManager?.activeNetwork
    val networkCapabilities = connectivityManager?.getNetworkCapabilities(currentNetwork)

    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

fun formatText(text: String): String {
    return text.trim().replace(",(?!\\s)".toRegex(), ", ")
        .replace("  ", " ")
        .replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }.run { if (endsWith('.')) this else "$this." }
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