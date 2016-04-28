package com.android.example.cinemaapp.app;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment{

    private static final String MOVIE_SHARE_HASHTAG = "powered by #CinemaApp";
    private String moviePosterStr;
    private int movieId;
    private HashMap<String, JSONObject> movieMapDetails;
    ImageView play_img;
    ImageView reviews_img;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentActivity detailFragmentActivity = getActivity();
        Intent intent = detailFragmentActivity.getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_activity_image);
        if (intent != null && intent.hasExtra("backDropPath") && intent.hasExtra("title") && intent.hasExtra("overview")) {
            moviePosterStr = intent.getStringExtra("backDropPath");
            //movieMapDetails = (HashMap<String, JSONObject>) intent.getSerializableExtra("movieDetailsMap");
            //  movieMapDetails = (HashMap<String, JSONObject>) intent.getParcelableExtra("movieDetailsMap");
            String title = intent.getStringExtra("title");
            movieId = intent.getIntExtra("id", 0);
            double popularity = intent.getDoubleExtra("popularity", 0.0);
            double rating = intent.getDoubleExtra("rating", 0.0);
            String overview = intent.getStringExtra("overview");
            Picasso.with(detailFragmentActivity).load(moviePosterStr).into(imageView);
            TextView tv_title = (TextView) rootView.findViewById(R.id.da_title);
            TextView tv_popularity = (TextView) rootView.findViewById(R.id.da_popularity);
            TextView tv_rating = (TextView) rootView.findViewById(R.id.da_rating);
            TextView tv_overview = (TextView) rootView.findViewById(R.id.da_overview);

            tv_title.setText(title);
            tv_popularity.setText(Double.toString(popularity));
            tv_rating.setText(Double.toString(rating));
            tv_overview.setText(overview);

        }

        play_img = (ImageView) rootView.findViewById(R.id.da_trailer_icon);
        play_img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                watchTrailer();
                // your code here
            }
        });

        reviews_img = (ImageView) rootView.findViewById(R.id.da_review_icon);
        reviews_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getActivity().getFragmentManager();
                Bundle args = new Bundle();
                args.putString("movieKey", Integer.toString(movieId));
                ReviewDialogFragment reviewsFragment = new ReviewDialogFragment();
                reviewsFragment.setArguments(args);
                reviewsFragment.show(fm,"hh");

                Toast.makeText(getActivity(), "Fetching reviews", Toast.LENGTH_LONG).show();
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider mshareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mshareActionProvider != null){
            mshareActionProvider.setShareIntent(createShareMovieIntent());
        }else{
            Log.d("DetailFragment", "ShareActionProvider is null");
        }

    }

    private Intent createShareMovieIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, moviePosterStr + " -" + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }

    public void watchTrailer(){
        Toast.makeText(getActivity(), "Navigating to youtube", Toast.LENGTH_LONG);
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=3A1LgFkIPfA")));
        Log.i("Video", "Video Playing....");

        new Thread(){
            public void run(){
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                String movieVideoJsonStr;

                try {

                    final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                    final String ID = Integer.toString(movieId);
                    final String VIDEOS = "videos";
                    final String API_KEY_PARAM = "api_key";

                    Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendEncodedPath(ID)
                            .appendEncodedPath(VIDEOS)
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                            .build();

                    Log.v("Wactch Trailer", builtUri.toString());
                    URL url = new URL(builtUri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        movieVideoJsonStr = null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {

                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        movieVideoJsonStr = null;
                    }
                    movieVideoJsonStr = buffer.toString();
                    Log.v("Watch Trailer", "Movie Video json "+movieVideoJsonStr);

                    try {
                        final String YOUTUBE_PATH = "http://www.youtube.com/watch?";
                        JSONObject jsonObject = new JSONObject(movieVideoJsonStr);
                        JSONArray results = jsonObject.getJSONArray("results");
                        String[] posterStrs = new String[results.length()];
                        JSONObject movieObj = (JSONObject) results.get(0);
                        final String V = "v";
                        Uri youtubeUri = Uri.parse(YOUTUBE_PATH).buildUpon()
                                .appendQueryParameter(V,movieObj.getString("key")).build();
                        startActivity(new Intent(Intent.ACTION_VIEW, youtubeUri));
                    }catch(JSONException je){
                        Log.e("Watch Trailer","Error while parsing JSON");
                    }
                } catch (IOException e) {
                    Log.e("Watch Trailer", "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attempting
                    // to parse it.
                    movieVideoJsonStr = null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("Watch Trailer", "Error closing stream", e);
                        }
                    }
                }
            }

        }.start();

    }
}




