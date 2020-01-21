package com.example.gworlddiningapp

//import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val PREFS_FILENAME: String = "gw-dining"

    private lateinit var default_radius: EditText
    private lateinit var find_restaurants: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //for saving parameters
        val preferences: SharedPreferences = getSharedPreferences("gw-dining", Context.MODE_PRIVATE)

        //get our values from the xml
        val restaurant1: CheckBox = findViewById(R.id.restaurant_1)
        val restaurant2: CheckBox = findViewById(R.id.restaurant_2)
        val restaurant3: CheckBox = findViewById(R.id.restaurant_3)
        val restaurant4: CheckBox = findViewById(R.id.restaurant_4)
        val restaurant5: CheckBox = findViewById(R.id.restaurant_5)
        val restaurant6: CheckBox = findViewById(R.id.restaurant_6)
        var restaurant_wild: CheckBox = findViewById(R.id.restaurant_wild)
        default_radius = findViewById(R.id.user_search_radius)
        find_restaurants = findViewById(R.id.find)

        //disable the button to make sure they have valid search criterea
        find_restaurants.isEnabled = false

        //set our initial values using sharedpreferences
        set_initial_prefs(preferences, restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6)

        //change state of checkbox
        change_checkbox_state(preferences, restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6)

        //get the specific food types
        val food_types:String? = get_food_types(restaurant1, restaurant2, restaurant3, restaurant4, restaurant5, restaurant6)

        //set search radius to stored value, and look for new value
        default_radius.setText(preferences.getString("SAVED_RADIUS", ""))
        default_radius.addTextChangedListener(textWatcher)

        find_restaurants.setOnClickListener {
            //set radius to the value we have, if it is empty then make it 1500
            if (default_radius.text.isBlank()) {
                default_radius.setText("1500")
                // Save the inputted username to file
                preferences
                    .edit()
                    .putString("SAVED_RADIUS", "1500")
                    .apply()
            }
            else{
                preferences
                    .edit()
                    .putString("SAVED_RADIUS", default_radius.text.toString())
                    .apply()
            }

            // Save the inputted username to file
            preferences
                .edit()
                .putString("SAVED_RADIUS", default_radius.text.toString())
                .apply()

            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("LOCATION", "Washington D.C.")

            intent.putExtra("RADIUS", default_radius.text.toString().trim())
            intent.putExtra("food_types", food_types)

            startActivity(intent)
        }

    }

    // A TextWatcher is an interface with three functions, so we cannot use lambda-shorthand
    // The functions are called accordingly as the user types in the EditText
    // https://developer.android.com/reference/android/text/TextWatcher
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(newString: CharSequence, start: Int, before: Int, count: Int) {
            val inputtedRadius: String = default_radius.text.toString().trim()
        }
    }

    //function for dealing with clicking checkboxes
    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked
            var find_restaurants: Button = findViewById(R.id.find)
//            val preferences = this.getSharedPreferences(PREFS_FILENAME, 0)
//            val editor = preferences.edit()

            if (checked){
                find_restaurants.setEnabled(true)
            }
            else {
                if(active_checkbox()){
                    find_restaurants.setEnabled(true)
                }
                else{
                    find_restaurants.setEnabled(false)
                }
            }

        }
    }

    fun active_checkbox(): Boolean{
        //get the restaurants
        var restaurant1: CheckBox = findViewById(R.id.restaurant_1)
        var restaurant2: CheckBox = findViewById(R.id.restaurant_2)
        var restaurant3: CheckBox = findViewById(R.id.restaurant_3)
        var restaurant4: CheckBox = findViewById(R.id.restaurant_4)
        var restaurant5: CheckBox = findViewById(R.id.restaurant_5)
        var restaurant6: CheckBox = findViewById(R.id.restaurant_6)
        var restaurant_wild: CheckBox = findViewById(R.id.restaurant_wild)

        //run through and check if any are checks
        if(restaurant1.isChecked || restaurant2.isChecked || restaurant3.isChecked ||
            restaurant4.isChecked || restaurant5.isChecked || restaurant6.isChecked || restaurant_wild.isChecked){
            return true
        }

        //otherwise return false
        return false
    }

    fun get_food_types(restaurant1: CheckBox, restaurant2: CheckBox, restaurant3: CheckBox, restaurant4: CheckBox, restaurant5: CheckBox, restaurant6: CheckBox): String? {
        var return_string: String = ""
        if(restaurant1.isChecked){
            return_string = return_string + getString(R.string.restaurant_1) + ","
        }

        if(restaurant2.isChecked){
            return_string = return_string + getString(R.string.restaurant_2) + ","
        }

        if(restaurant3.isChecked){
            return_string = return_string + getString(R.string.restaurant_3) + ","
        }

        if(restaurant4.isChecked){
            return_string = return_string + getString(R.string.restaurant_4) + ","
        }

        if(restaurant5.isChecked){
            return_string = return_string + getString(R.string.restaurant_5) + ","
        }

        if(restaurant6.isChecked){
            return_string = return_string + getString(R.string.restaurant_6) + ","
        }

        //check if last char is ','
        if(return_string.get(return_string.length-1) == ','){
            return_string = return_string.substring(0, return_string.length-1)
        }


        //if return string null, send all of them
        if(return_string == ""){
            return_string = getString(R.string.restaurant_1) + "," + getString(R.string.restaurant_2) + "," + getString(R.string.restaurant_3) + "," +
                    getString(R.string.restaurant_4) + "," + getString(R.string.restaurant_5) + "," + getString(R.string.restaurant_6)
        }

        Log.e("return_string", return_string)

        return return_string
    }

    fun set_initial_prefs(preferences: SharedPreferences, restaurant1: CheckBox, restaurant2: CheckBox, restaurant3: CheckBox, restaurant4: CheckBox, restaurant5: CheckBox, restaurant6: CheckBox){
        if(preferences.getBoolean("restaurant1", true)){
            restaurant1.setChecked(true)
            find_restaurants.isEnabled = true
        }

        if(preferences.getBoolean("restaurant2", true)){
            restaurant2.setChecked(true)
            find_restaurants.isEnabled = true
        }

        if(preferences.getBoolean("restaurant3", true)){
            restaurant3.setChecked(true)
            find_restaurants.isEnabled = true
        }

        if(preferences.getBoolean("restaurant4", true)){
            restaurant4.setChecked(true)
            find_restaurants.isEnabled = true
        }

        if(preferences.getBoolean("restaurant5", true)){
            restaurant5.setChecked(true)
            find_restaurants.isEnabled = true
        }
        if(preferences.getBoolean("restaurant6", true)){
            restaurant6.setChecked(true)
            find_restaurants.isEnabled = true
        }
    }

    fun change_checkbox_state(preferences: SharedPreferences, restaurant1: CheckBox, restaurant2: CheckBox, restaurant3: CheckBox, restaurant4: CheckBox, restaurant5: CheckBox, restaurant6: CheckBox){
        restaurant1.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (restaurant1.isChecked()) {
                preferences.edit()
                    .putBoolean("restaurant1", true)
                    .apply()
            } else {
                preferences.edit()
                    .putBoolean("restaurant1", false)
                    .apply()
            }
        })

        restaurant2.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (restaurant2.isChecked()) {
                preferences.edit()
                    .putBoolean("restaurant2", true)
                    .apply()
            } else {
                preferences.edit()
                    .putBoolean("restaurant2", false)
                    .apply()
            }
        })

        restaurant3.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (restaurant3.isChecked()) {
                preferences.edit()
                    .putBoolean("restaurant3", true)
                    .apply()
            } else {
                preferences.edit()
                    .putBoolean("restaurant3", false)
                    .apply()
            }
        })

        restaurant4.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (restaurant4.isChecked()) {
                preferences.edit()
                    .putBoolean("restaurant4", true)
                    .apply()
            } else {
                preferences.edit()
                    .putBoolean("restaurant4", false)
                    .apply()
            }
        })

        restaurant5.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (restaurant5.isChecked()) {
                preferences.edit()
                    .putBoolean("restaurant5", true)
                    .apply()
            } else {
                preferences.edit()
                    .putBoolean("restaurant5", false)
                    .apply()
            }
        })

        restaurant6.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, b ->
            if (restaurant6.isChecked()) {
                preferences.edit()
                    .putBoolean("restaurant6", true)
                    .apply()
            } else {
                preferences.edit()
                    .putBoolean("restaurant6", false)
                    .apply()
            }
        })
    }
}
