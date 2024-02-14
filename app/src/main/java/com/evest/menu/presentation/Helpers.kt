package com.evest.menu.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import androidx.wear.activity.ConfirmationActivity
import com.evest.menu.R
import okhttp3.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

fun getMealType(string: String): String? {
    return when (string) {
        "Snídaně" -> "breakfast"
        "Polévka" -> "soup"
        "Oběd1" -> "lunch1"
        "Oběd2" -> "lunch2"
        "Večeře" -> "dinner"
        else -> null
    }
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

fun getNewestOldestDateString(dateStringList: List<String>): Pair<String, String> {
    return dateStringList.fold("" to "") { acc, dateString ->
        acc.let { (currentOldest, currentNewest) ->
            val currentDate = LocalDate.parse(dateString)
            val newOldest =
                if (currentOldest.isEmpty() || currentDate < LocalDate.parse(currentOldest)) dateString else currentOldest
            val newNewest =
                if (currentNewest.isEmpty() || currentDate > LocalDate.parse(currentNewest)) dateString else currentNewest
            newOldest to newNewest
        }
    }
}

fun getCookieValue(response: Response, cookieName: String): String? {
    return response.headers("Set-Cookie")
        .find { it.startsWith(cookieName) }
        ?.substringAfter("$cookieName=")
        ?.substringBefore(";")
}

fun parseTimeBetweenTags(input: String, tag: String): LocalTime? {
    val cleanInput = input.replace(" ", " ")
    val regex = "$tag <b>\\s*([^<]+)</b>".toRegex()
    val matchResult =
        regex.find(cleanInput) ?: return null

    val timeString = matchResult.groupValues[1]
    return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"))
}

fun parseDateTimeBetweenTags(input: String, tag: String): LocalDateTime? {
    val cleanInput = input.replace(" ", " ")
    val regex = "$tag <b>\\s*([^<]+)</b>".toRegex()
    val matchResult =
        regex.find(cleanInput) ?: return null

    val dateTimeString = matchResult.groupValues[1]
    return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
}
