package com.android.example.cinemaapp.app;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by V on 4/12/2016.
 */
public class ReviewDialogFragment extends DialogFragment{

    String movieKey;
    ReviewAdapter reviewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        getDialog().setTitle("Reviews");
        movieKey = getArguments().getString("movieKey");

        reviewAdapter = new ReviewAdapter(getActivity(), new ArrayList());

        ListView lView = (ListView) rootView.findViewById(R.id.review_list);

        lView.setAdapter(reviewAdapter);
        new FetchMovieReviewsTask().execute(movieKey);

        Button b_close = (Button) rootView.findViewById(R.id.close);
        b_close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return rootView;
    }

    public class FetchMovieReviewsTask extends AsyncTask<String, Void, List<Reviews>> {

        private final ProgressDialog dialog = new ProgressDialog(getActivity());

        @Override
        protected List<Reviews> doInBackground(String... params) {
            Log.v("FetchMovieReviewTask", "In background method");

            List<Reviews> reviewsList = new ArrayList<Reviews>();
            // These two need to be declared outside the try/catch
            HttpURLConnection urlConnection = null;
            // so that they can be closed in the finally block.
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieReviewJsonStr;

            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String MOVIE_KEY = params[0];
                final String REVIEWS_PARAM = "reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendEncodedPath(MOVIE_KEY)
                        .appendEncodedPath(REVIEWS_PARAM)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    movieReviewJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    movieReviewJsonStr = null;
                }
                movieReviewJsonStr = buffer.toString();
//                Log.v("MainActivityFragment", "Movie json "+movieJsonStr);

                try {
                    JSONObject jsonObject = new JSONObject(movieReviewJsonStr);
                    JSONArray reviewArr = jsonObject.getJSONArray("results");
                    for(int i=0; i<reviewArr.length(); i++){
                        reviewsList.add(convertReview(reviewArr.getJSONObject(i)));
                    }
                    return reviewsList;
                }catch(JSONException je){
                    Log.e("MovieReviewPath","Error while parsing JSON");
                }
            } catch (IOException e) {
                Log.e("MovieReviewAdapter", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                movieReviewJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MoviePosterAdapter", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        private Reviews convertReview(JSONObject reviewObj) throws JSONException{
            String author = reviewObj.getString("author");
            String reviewContent = reviewObj.getString("content");

            return new Reviews(author, reviewContent);
        }

        @Override
        protected void onPostExecute(List<Reviews> reviewsList) {
            super.onPostExecute(reviewsList);
            dialog.dismiss();
            reviewAdapter.setItemList(reviewsList);
            reviewAdapter.notifyDataSetChanged();
        }


        @Override
        protected void onPreExecute () {
            super.onPreExecute();
            dialog.setMessage("loading reviews...");
            dialog.show();
        }
    }
}
