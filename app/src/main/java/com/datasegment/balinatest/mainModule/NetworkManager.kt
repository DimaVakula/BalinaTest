package com.datasegment.balinatest.mainModule

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class NetworkManager {
    private val client = OkHttpClient()
    private val baseURL = "https://junior.balinasoft.com/api/"

    fun uploadPhoto(token: String, photo:String, date: Int, lat:Double, lng:Double, callback: (String?) -> Unit) {
        val url = baseURL + "image"

        val json = JSONObject().apply {
            put("base64Image", photo)
            put("date", date)
            put("lat",lat)
            put("lng",lng)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .header("Access-Token", value = token)
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

    fun getPhoto(token: String, page: Int, callback: (String?) -> Unit) {
        val url = baseURL + "image?page="+page
        val request = Request.Builder()
            .header("Access-Token", value = token)
            .url(url)
            .get()
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

    fun delPhoto(token: String, id: Int, callback: (String?) -> Unit) {
        val url = baseURL + "image/"+id
        val request = Request.Builder()
            .header("Access-Token", value = token)
            .url(url)
            .delete()
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

    fun uploadComment(token: String, text:String, idPhoto:Int, callback: (String?) -> Unit) {
        val url = baseURL + "image/" + idPhoto + "/comment"

        val json = JSONObject().apply {
            put("text", text)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .header("Access-Token", value = token)
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

    fun getComment(token: String,idPhoto:Int, page:Int, callback: (String?) -> Unit) {
        val url = baseURL + "image/" + idPhoto + "/comment?page="+page
        val request = Request.Builder()
            .header("Access-Token", value = token)
            .url(url)
            .get()
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

    fun delComment(token: String,idPhoto:Int, idComment:Int, callback: (String?) -> Unit) {
        val url = baseURL + "image/" + idPhoto + "/comment/" + idComment
        val request = Request.Builder()
            .header("Access-Token", value = token)
            .url(url)
            .delete()
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