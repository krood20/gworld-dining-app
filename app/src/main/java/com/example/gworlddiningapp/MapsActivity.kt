package com.example.gworlddiningapp

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdateFactory.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import org.jetbrains.anko.doAsync
import edu.gwu.gworlddiningapp.Review
import okhttp3.Request
import org.json.JSONObject
import okhttp3.OkHttpClient
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.model.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var confirm: Button

    private lateinit var yelp_manager: YelpManager

    private lateinit var gworld_manager: GworldManager

    // We don't have to supply a value here, since we have an init block.
    private lateinit var okHttpClient: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // Triggers the map to load, will call onMapReady when complete
        mapFragment.getMapAsync(this)

        confirm = findViewById(R.id.confirm)
        confirm.isEnabled = false

        //lets do something if it is the first time running
        var firstRun: Boolean = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstRun", true)
        if(firstRun){
            first_time_screen()
        }

    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //initilize keys
        val yelp_api_key = getString(R.string.yelp_key)
        //val token = yelp_manager.retrieveOAuthToken(yelp_api_key)
        var yelp_manager = YelpManager()
        var gworld_manager = GworldManager()

        mMap = googleMap

        //get our search radius
        var rad  = intent.getStringExtra("RADIUS")
        if(TextUtils.isEmpty(rad)){
            rad = "1500"
        }
        var radius = rad.toDouble()
        Log.d("RADIUS", radius.toString())



        val gw_lat_lng = LatLng(38.899878, -77.049152)

        // Pan the camera over to the map marker and zoom in
        val zoomLevel = 12.0f
        mMap.animateCamera(
            newLatLngZoom(gw_lat_lng, zoomLevel)
        )

        // Place a map marker
        val gw_name = "The George Washington University"
        val gw_rating = "Rating: 3.5"
        mMap.addMarker(
            MarkerOptions().position(gw_lat_lng).title(gw_name).snippet(gw_rating)
        )

        //get food types and parse it
        val food_types = intent.getStringExtra("food_types")

        mMap.setOnMapLongClickListener { latLng ->
            mMap.clear()

            // Start running some code on the background
            doAsync {
                try {
                    var businesses: List<Business> = yelp_manager.retrieveBusinesses(
                        yelp_api_key,
                        latLng,
                        radius.toInt(),
                        food_types
                    )

                    //get our list of gworld businesses
                    var gworld_businesses: List<Gworld> =
                        gworld_manager.retrieveGworldBusinesses("name", "address")

                    //check gworld businesses
                    check_businesses(gworld_businesses, businesses)

                    // Switch back to UI thread to update the UI
                    runOnUiThread {
                        if (businesses.isNotEmpty()) {
                            //drawing circle wirth radius you entered on first screen
                            mMap.addCircle(
                                CircleOptions()
                                    .center(latLng)
                                    .radius(radius)
                                    .strokeColor(Color.RED)
                            )

                            // Pan the camera over to the map marker and zoom in
                            val zoomLevel = 12.0f
                            mMap.animateCamera(
                                newLatLngZoom(latLng, zoomLevel)
                            )

                            //place map markers
                            for (i in 0 until businesses.size) {
                                //check if it is one of the categories
                                Log.e("businesses", businesses[i].toString())
                                if (businesses[i].gworld) {
                                    Log.e("GWORLD", "GWORLD")
                                    mMap.addMarker(
                                        MarkerOptions().position(
                                            LatLng(
                                                businesses[i].lat,
                                                businesses[i].lng
                                            )
                                        ).title(businesses[i].name).icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE
                                            )
                                        ).snippet("Rating" + businesses[i].rating.toString())
                                    )
                                } else {
                                    mMap.addMarker(
                                        MarkerOptions().position(
                                            LatLng(
                                                businesses[i].lat,
                                                businesses[i].lng
                                            )
                                        ).title(businesses[i].name).snippet("Rating: " + businesses[i].rating.toString())
                                    )
                                }
                            }

                            //if the marker isnt null, get business id
                            val inten = Intent(this@MapsActivity, ReviewsActivity::class.java)

                            mMap.setOnMarkerClickListener { marker ->
                                //setMarkerOnClickListener
                                //get marker that was clicked
                                val clicked_marker =
                                    businesses.find { business -> business.name == marker.title }

                                if (clicked_marker != null) {
                                    inten.putExtra("business_id", clicked_marker.business_id)
                                    inten.putExtra("url", clicked_marker.url)
                                    updateButton(clicked_marker.name)
                                }

                                false
                            }

                            confirm.setOnClickListener {
                                startActivity(inten)
                            }

                        } else {
                            // The UI can only be updated from the UI Thread
                            Toast.makeText(
                                this@MapsActivity,
                                "Error retrieving Businesses",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }catch(e: Exception) {
                    // The UI can only be updated from the UI Thread
                    runOnUiThread {
                        Toast.makeText(
                            this@MapsActivity,
                            "Error retrieving businesses",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }
    }

    private fun updateButton(name: String) {
        // Update the button color -- need to load the color from resources first
        val greenColor = ContextCompat.getColor(
            this, R.color.buttonGreen
        )
        val checkIcon = ContextCompat.getDrawable(
            this, R.drawable.ic_check_white
        )
        confirm.setBackgroundColor(greenColor)
        // Update the left-aligned icon
        confirm.setCompoundDrawablesWithIntrinsicBounds(checkIcon, null, null, null)
        confirm.isEnabled = true

        confirm.apply{
            text=getString(R.string.map_button_update) +  " " + name
        }
    }

    fun check_businesses(gworld_businesses: List<Gworld>, businesses: List<Business>){
        //iterate over the businesses
        for(business in businesses){
            for(gworld in gworld_businesses){
                if(business.address == gworld.address){
                    business.gworld = true
                }
            }

        }
    }

    fun first_time_screen(){
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("firstRun", false).commit()

        AlertDialog.Builder(this)
            .setTitle("Welcome to The GW Dining Map")
            .setMessage("You can long press on the screen to show restaurants within the radius you typed on th first screen!\n\n" +
                    "The markers shaded blue are available for GWorld purchases!\n\n" +
                    "If you press the bottom button, you will be taken to a page of reviews for that restaurant!")
            .setPositiveButton("Lets get Started!"){butt, butt2 ->
                //set to ok
            }
            .show()
    }

}