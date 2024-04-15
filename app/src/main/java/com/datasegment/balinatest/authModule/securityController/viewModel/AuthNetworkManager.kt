package com.datasegment.balinatest.authModule.securityController.viewModel

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AuthNetworkManager {
    private val client = OkHttpClient()
    private val baseURL = "https://junior.balinasoft.com/api/"
    fun sendRegistrationRequest(login: String, password: String, callback: (String?) -> Unit) {
        val url = baseURL + "account/signup"
        val json = JSONObject().apply {
            put("login", login)
            put("password", password)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                callback(responseData)
            }
        })
    }

    fun sendLoginRequest(login: String, password: String, callback: (String?) -> Unit) {
        val url = baseURL + "account/signin"
        val json = JSONObject().apply {
            put("login", login)
            put("password", password)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                callback(responseData)
            }
        })
    }
}
