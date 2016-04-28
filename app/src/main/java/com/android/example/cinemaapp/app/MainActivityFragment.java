package com.android.example.cinemaapp.app;

        import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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
public class MainActivityFragment extends Fragment {

    MoviePosterAdapter moviePosterAdapter;
    GridView posterGridView;
    public HashMap<String, JSONObject> movieMap;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        //updateMovies();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateMovies();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovies();

        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovies(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPreferences.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popular));
        moviePosterAdapter.clear();
        new FetchMoviePosterTask().execute(sortBy);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        moviePosterAdapter = new MoviePosterAdapter(getActivity());
        posterGridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        //updateMovies();
        posterGridView.setAdapter(moviePosterAdapter);
        posterGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MainActivityFragment mf = new MainActivityFragment();
                String moviePoster = (String) moviePosterAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                JSONObject movieObj = movieMap.get(moviePoster);

                try {
                    String backDropPath = "http://image.tmdb.org/t/p/w342" + movieObj.getString("backdrop_path");
                    intent.putExtra("id", movieObj.getInt("id"));
                    intent.putExtra("backDropPath", backDropPath);
                    intent.putExtra("title", movieObj.getString("title"));
                    intent.putExtra("overview", movieObj.getString("overview"));
                    intent.putExtra("popularity", movieObj.getDouble("popularity"));
                    intent.putExtra("rating", movieObj.getDouble("vote_average"));
                } catch (JSONException je) {
                    Log.e("Onclick", "Error while parsing");
                }

                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchMoviePosterTask extends AsyncTask<String, String, Void> {


        @Override
        protected Void doInBackground(String... params) {
            Log.v("FetchMoviePosterTask", "In background method");
            // These two need to be declared outside the try/catch
            HttpURLConnection urlConnection = null;
            // so that they can be closed in the finally block.
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr;

            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String SORT_PARAM = params[0];
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendEncodedPath(SORT_PARAM)
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
                    movieJsonStr = null;
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
                    movieJsonStr = null;
                }
                movieJsonStr = buffer.toString();
//                Log.v("MainActivityFragment", "Movie json "+movieJsonStr);

                try {
                    String[] posterPaths = getPosterPaths(movieJsonStr);
                    for(String path : posterPaths){
                        publishProgress(path);
                    }
                }catch(JSONException je){
                    Log.e("MoviePosterPath","Error while parsing JSON");
                }
            } catch (IOException e) {
                Log.e("MoviePosterAdapter", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                movieJsonStr = null;
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

        public String[] getPosterPaths(String movieJsonStr) throws JSONException{
            final String POSTER_PATH = "http://image.tmdb.org/t/p/";
            final String POSTER_SIZE = "w185/";
            JSONObject jsonObject = new JSONObject(movieJsonStr);
            JSONArray results = jsonObject.getJSONArray("results");
            String[] posterStrs = new String[results.length()];
            movieMap = new HashMap<String, JSONObject>();

            for(int i =0; i < results.length(); i++){
                JSONObject movieObj = (JSONObject) results.get(i);
                posterStrs[i] = POSTER_PATH + POSTER_SIZE + movieObj.getString("poster_path");
                movieMap.put(posterStrs[i], movieObj);
            }

            return posterStrs;
        }

        @Override
        protected void onProgressUpdate(String... posterValues){
            Log.v("FetchMoviePosterTask", "In onProgress method");
            moviePosterAdapter.add(posterValues[0]);
            super.onProgressUpdate(posterValues);
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v("FetchMoviePosterTask", "In onPostExecute method");
            moviePosterAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }
}