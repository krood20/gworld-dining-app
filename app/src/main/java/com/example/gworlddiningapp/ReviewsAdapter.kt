package com.example.gworlddiningapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.gwu.gworlddiningapp.Review

class ReviewsAdapter(val reviews: List<Review>) : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    // onCreateViewHolder is called when the RecyclerView needs a new XML layout to be loaded for a row
    // Open & parse our XML file for our row and return the ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        // Open & parse our XML file
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_review, parent, false)

        // Create a new ViewHolder
        return ReviewsViewHolder(view)
    }

    // Returns the number of rows to render
    override fun getItemCount(): Int {
        return reviews.size
    }

    // onBindViewHolder is called when the RecyclerView is ready to display a new row at [position]
    // and needs you to fill that row with the necessary data.
    //
    // It passes you a ViewHolder, either from what you returned from onCreateViewHolder *or*
    // it's passing you an existing ViewHolder as a part of the "recycling" mechanism.
    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val currentReview = reviews[position]

        val rate: Float = currentReview.rating.toFloat()
        holder.rating.rating = rate
        holder.name.text = currentReview.name
        holder.review_text.text = currentReview.review_text
        //holder.review_url.text = currentReview.review_url

        // Uncomment to turn on debug indicators
        // Picasso
        //     .get()
        //     .setIndicatorsEnabled(true)

        // Load the profile picture into our icon ImageView
//        Picasso
//            .get()
//            .load(currentTweet.iconUrl)
//            .into(holder.icon)
    }

    // A ViewHolder is a class which *holds* references to *views* that we care about in each
    // individual row. The findViewById function is somewhat inefficient, so the idea is to the lookup
    // for each view once and then reuse the object.
    class ReviewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val icon: ImageView = view.findViewById(R.id.icon)
        val rating: RatingBar = view.findViewById(R.id.rating)

        val name: TextView = view.findViewById(R.id.name)

        val review_text: TextView = view.findViewById(R.id.review_text)

        //val review_url: TextView = view.findViewById(R.id.review_url)
    }
}