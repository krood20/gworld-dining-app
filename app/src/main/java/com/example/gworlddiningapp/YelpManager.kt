package com.example.gworlddiningapp

import android.util.Base64
import com.example.gworlddiningapp.Business
import com.google.android.gms.maps.model.LatLng
import edu.gwu.gworlddiningapp.Review
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import android.util.Log
import android.widget.Toast


class YelpManager {

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

    // Twitter's APIs are protected by OAuth. APIs from other companies (like Yelp) do not require OAuth.
    fun retrieveOAuthToken(
        apiKey: String,
        apiSecret: String
    ): String {

        // Encoding for a URL -- converts things like spaces into %20
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val encodedSecret = URLEncoder.encode(apiSecret, "UTF-8")

        // Concatenate the two together, with a colon inbetween
        val combinedEncoded = "$encodedKey:$encodedSecret"

        // Base-64 encode the combined string
        // https://en.wikipedia.org/wiki/Base64
        val base64Combined = Base64.encodeToString(
            combinedEncoded.toByteArray(), Base64.NO_WRAP)

        // OAuth is a POST API call, so we need to create the actual "body" of the request (e.g.
        // the data we want to send). This request body is specific to an OAuth call.
        val requestBody = "grant_type=client_credentials"
            .toRequestBody(
                contentType = "application/x-www-form-urlencoded".toMediaType()
            )

        val request = Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .header("Authorization", "Basic $base64Combined")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseString = response.body?.string()

        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse JSON
            val json = JSONObject(responseString)
            val token = json.getString("access_token")

            return token
        } else {
            // API request failed - ideally we should think about returning null or an Exception
            return ""
        }

        // One improvement we can make is also to cache the OAuth token, since it can be reused for all Twitter API
        // requests in this session.
    }

    fun retrieveReviews(
        oAuthToken: String,
        id: String
    ): List<Review> {
        // Build our request to turn - for now, using a hardcoded OAuth token
        val request = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/$id/reviews")
            .header("Authorization", "Bearer $oAuthToken")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseString: String? = response.body?.string()

        val reviews = mutableListOf<Review>()

        // Confirm that we retrieved a successful (e.g. 200) response with some body content
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse the JSON response that was sent back by the server
            val jsonObject = JSONObject(responseString)
            val statuses = jsonObject.getJSONArray("reviews")

            for (i in 0 until statuses.length()) {
                val reviewJson = statuses.getJSONObject(i)

                // Get the Review's content
                val review_text = reviewJson.getString("text")
                val rating = reviewJson.getString("rating")
                val review_url = reviewJson.getString("url")

                val user: JSONObject = reviewJson.getJSONObject("user")
                val name = user.getString("name")

                val review = Review(
                    name = name,
                    rating = rating,
                    review_text = review_text,
                    review_url = review_url
                )

                reviews.add(review)
            }
        }

        return reviews

    }

    fun retrieveBusinesses(
        oAuthToken: String,
        latLng: LatLng,
        radius: Int,
        categories:String
    ): List<Business> {

        // Data setup
        val latitude = latLng.latitude
        val longitude = latLng.longitude

        // Build our request to turn - for now, using a hardcoded OAuth token
        val request = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/search?latitude=$latitude&longitude=$longitude&radius=$radius&categories=$categories")
            .header("Authorization", "Bearer $oAuthToken")
            .build()

        // Calling .execute actually makes the network request, blocking the thread until the server
        // responds, or the request times out.
        //
        // If there are any connection or network errors, .execute will throw an Exception.
        val response = okHttpClient.newCall(request).execute()
        val responseString: String? = response.body?.string()

        val businesses = mutableListOf<Business>()

        // Confirm that we retrieved a successful (e.g. 200) response with some body content
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse the JSON response that was sent back by the server
            val jsonObject = JSONObject(responseString)
            val statuses = jsonObject.getJSONArray("businesses")

            for (i in 0 until statuses.length()) {
                val businessJson = statuses.getJSONObject(i)

                // Get the Tweet's content
                val coordinates = businessJson.getJSONObject("coordinates")
                val lat = coordinates.getDouble("latitude")
                val lng = coordinates.getDouble("longitude")
                val name = businessJson.getString("name")
                val business_id = businessJson.getString("id")
                val url = businessJson.getString("url")
                val location: JSONObject = businessJson.getJSONObject("location")
                val address: String = location.getString("address1")
                val rating: Double = businessJson.getDouble("rating")
                val gworld = false

                val business = Business(
                    business_id = business_id,
                    name = name,
                    url = url,
                    lat = lat,
                    lng = lng,
                    address = address,
                    rating=rating,
                    gworld = gworld
                )

                businesses.add(business)
            }
        }

        return businesses
    }

}