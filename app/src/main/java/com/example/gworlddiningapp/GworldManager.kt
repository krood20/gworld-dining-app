package com.example.gworlddiningapp

import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import android.util.Log


class GworldManager {

    // We don't have to supply a value here, since we have an init block.
    private val okHttpClient: OkHttpClient

    // An init block is similar to having a longer constructor in Java - it allows us to run
    // extra code during initialization. All variables must be set by the end of the init block.
    init {
        // Turn on console logging for our network traffic, useful during development
        val builder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        builder.addInterceptor(logging)
        okHttpClient = builder.build()
    }

    fun retrieveGworldBusinesses(
        name: String,
        address: String
    ): List<Gworld> {
        // Build our request to turn - for now, using a hardcoded OAuth token
        val request = Request.Builder()
            //.url("http://www.mocky.io/v2/5d7e80913300008e00f0ad94/search?name=$name&address=$address")
            .url("http://www.mocky.io/v2/5d7e80913300008e00f0ad94")
            .build()

        // Calling .execute actually makes the network request, blocking the thread until the server
        // responds, or the request times out.
        //
        // If there are any connection or network errors, .execute will throw an Exception.
        val response = okHttpClient.newCall(request).execute()
        val responseString: String? = response.body?.string()

        val gworlds = mutableListOf<Gworld>()

        // Confirm that we retrieved a successful (e.g. 200) response with some body content
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse the JSON response that was sent back by the server
            val jsonObject = JSONObject(responseString)
            val statuses = jsonObject.getJSONArray("gworld")

            for (i in 0 until statuses.length()) {
                val reviewJson = statuses.getJSONObject(i)

                val name = reviewJson.getString("name")
                val address = reviewJson.getString("address")

                val gworld = Gworld(
                    name=name,
                    address = address
                )

                gworlds.add(gworld)
            }
        }

        return gworlds
    }

}