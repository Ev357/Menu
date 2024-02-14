package com.evest.menu.presentation

import android.content.Context
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

fun login(
    context: Context,
    username: String? = null,
    password: String? = null
): Pair<String, String> {
    return if (AppData.jSessionId.isEmpty() || AppData.rememberMe.isEmpty()) {
        getJSessionIdAndRememberMe(context, username, password)
    } else {
        val cryptoManager = CryptoManager()

        println(AppData.jSessionId)
        println(AppData.rememberMe)

        val jSessionId = cryptoManager.decrypt(AppData.jSessionId)
        val rememberMe = cryptoManager.decrypt(AppData.rememberMe)

        println(jSessionId)
        println(rememberMe)

        jSessionId to rememberMe
    }
}

private fun getJSessionIdAndRememberMe(
    context: Context,
    username: String? = null,
    password: String? = null
): Pair<String, String> {
    val serverPreferences =
        context.getSharedPreferences(Preference.ServerPreference.name, Context.MODE_PRIVATE)
    val urlOption = Preference.ServerPreference.options.first()

    var theUsername = username
    var thePassword = password

    val cryptoManager = CryptoManager()

    if (theUsername.isNullOrEmpty() || thePassword.isNullOrEmpty()) {
        val accountPreferences =
            context.getSharedPreferences(Preference.AccountPreference.name, Context.MODE_PRIVATE)

        val usernameOption = Preference.AccountPreference.options[1]
        val passwordOption = Preference.AccountPreference.options[2]

        val encryptedUsername =
            accountPreferences.getString(usernameOption.name, usernameOption.defaultValue as String)
                ?: failLogin()

        val encryptedPassword =
            accountPreferences.getString(passwordOption.name, passwordOption.defaultValue as String)
                ?: failLogin()

        try {
            theUsername = cryptoManager.decrypt(encryptedUsername)
            thePassword = cryptoManager.decrypt(encryptedPassword)
        } catch (e: Throwable) {
            failLogin()
        }
    }

    val url =
        serverPreferences.getString(urlOption.name, urlOption.defaultValue as String)
            ?: failLogin()

    val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false)
        .build()

    val getRequest = Request.Builder()
        .url(url)
        .build()

    val getResponse = client.newCall(getRequest).execute()

    val initialJSessionId = getCookieValue(getResponse, "JSESSIONID")
    val initialXsrfToken = getCookieValue(getResponse, "XSRF-TOKEN") ?: failLogin()

    val formBody = FormBody.Builder()
        .add("j_username", theUsername)
        .add("j_password", thePassword)
        .add("_spring_security_remember_me", "on")
        .add("terminal", "false")
        .add("_csrf", initialXsrfToken)
        .add("targetUrl", "/faces/secured/main.jsp?terminal=false&status=true&printer=&keyboard=")
        .build()

    val postRequest = Request.Builder()
        .url("$url/j_spring_security_check")
        .post(formBody)
        .header("cookie", "XSRF-TOKEN=$initialXsrfToken; JSESSIONID=$initialJSessionId")
        .build()

    val postResponse = client.newCall(postRequest).execute()

    val jSessionId = getCookieValue(postResponse, "JSESSIONID")
    val rememberMe = getCookieValue(postResponse, "remember-me")

    if (jSessionId.isNullOrEmpty() || rememberMe.isNullOrEmpty()) {
        failLogin()
    }

    AppData.jSessionId = cryptoManager.encrypt(jSessionId)
    AppData.rememberMe = cryptoManager.encrypt(rememberMe)

    return jSessionId to rememberMe
}

private fun failLogin(): Nothing {
    throw Exception("Login Failed")
}