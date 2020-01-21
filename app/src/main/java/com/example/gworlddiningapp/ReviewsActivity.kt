package com.example.gworlddiningapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import edu.gwu.gworlddiningapp.Review
import kotlinx.android.synthetic.main.activity_reviews.*
import org.jetbrains.anko.doAsync

class ReviewsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        //get button info
        button = findViewById(R.id.confirm)
        button.isEnabled = true

        //get important strings
        recyclerView = findViewById(R.id.recyclerView)

        // Set the direction of our list to be vertical
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Networking must be done on a background thread
        doAsync {
            // Our networking functions can throw an Exception on connection / network errors
            try {
                val reviewManager = YelpManager()

                // Read our API key / secret from XML
                val apiKey = getString(R.string.yelp_key)
                var bid  = intent.getStringExtra("business_id")

                val revList: List<Review> = reviewManager.retrieveReviews(
                    oAuthToken = apiKey,
                    id = bid
                )

                // The UI can only be updated from the UI Thread
                runOnUiThread {
                    if(revList.isNotEmpty()) {
                        // Create the adapter and assign it to the RecyclerView
                        recyclerView.adapter = ReviewsAdapter(revList)

                        val url = intent.getStringExtra("url")

                        //initialize web intent
                        val webIntent: Intent = Uri.parse(url).let { webpage ->
                            Intent(Intent.ACTION_VIEW, webpage)
                        }
                        button.setOnClickListener {
                            Log.e("INTENT", "CLICKED")
                            startActivity(webIntent)
                        }
                    }
                    else{
                        Toast.makeText(
                            this@ReviewsActivity,
                            "Error retrieving reviews",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch(e: Exception) {
                // The UI can only be updated from the UI Thread
                runOnUiThread {
                    Toast.makeText(
                        this@ReviewsActivity,
                        "Error retrieving reviews",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }

}
