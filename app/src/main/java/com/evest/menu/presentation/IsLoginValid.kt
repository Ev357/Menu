package com.evest.menu.presentation

import android.content.Context
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

fun isLoginValid(username: String, password: String, context: Context): Boolean {
    val serverPreferences =
        context.getSharedPreferences(Preference.ServerPreference.name, Context.MODE_PRIVATE)
    val urlOption = Preference.ServerPreference.options.first()

    val url =
        serverPreferences.getString(urlOption.name, urlOption.defaultValue as String)
            ?: return false

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
    val initialXsrfToken = getCookieValue(getResponse, "XSRF-TOKEN") ?: return false

    val formBody = FormBody.Builder()
        .add("j_username", username)
        .add("j_password", password)
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

    getCookieValue(postResponse, "JSESSIONID") ?: return false

    return true
}