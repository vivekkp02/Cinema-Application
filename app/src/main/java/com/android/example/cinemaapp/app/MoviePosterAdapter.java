package com.android.example.cinemaapp.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by V on 3/27/2016.
 */
public class MoviePosterAdapter extends BaseAdapter {

    ArrayList<String> movieArray = new ArrayList<String>();
    private Context mContext;

    public MoviePosterAdapter(Context c) {
        mContext = c;
    }

    void add(String path){
        movieArray.add(path);
    }

    void clear(){
        movieArray.clear();
    }

    void remove(int index){
        movieArray.remove(index);
    }

    @Override
    public int getCount() {
        return movieArray.size();
    }

    @Override
    public Object getItem(int position) {
        return movieArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);

            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext).load(movieArray.get(position)).into(imageView);
        return imageView;
    }
}
