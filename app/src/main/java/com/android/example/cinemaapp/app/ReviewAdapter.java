package com.android.example.cinemaapp.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by V on 4/16/2016.
 */
public class ReviewAdapter extends ArrayAdapter<Reviews>{

    private List<Reviews> reviewList;
    private Context context;


    public ReviewAdapter(Context context, List<Reviews> reviewList){
        super(context, 0, reviewList);
        this.reviewList = reviewList;
        this.context = context;
    }

    public int getCount(){
        if(reviewList != null){
            return reviewList.size();
        }
        return 0;
    }

    public Reviews getItem(int position){
        if(reviewList != null){
            return  reviewList.get(position);
        }
        return null;
    }

    public long getItemId(int position) {
        if (reviewList != null)
            return reviewList.get(position).hashCode();
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Reviews review = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }
        // Lookup view for data population
        TextView tv_review = (TextView) convertView.findViewById(R.id.tv_review);
        TextView tv_author = (TextView) convertView.findViewById(R.id.tv_author);
        // Populate the data into the template view using the data object
        tv_review.setText(review.getReviewContent());
        tv_author.setText(review.getAuthor());
        // Return the completed view to render on screen
        return convertView;
    }

    public List<Reviews> getItemList() {
        return reviewList;
    }

    public void setItemList(List<Reviews> reviewList) {
        this.reviewList = reviewList;
    }
}
