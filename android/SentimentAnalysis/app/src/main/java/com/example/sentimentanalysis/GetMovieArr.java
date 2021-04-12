package com.example.sentimentanalysis;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class GetMovieArr extends AsyncTask<String, Void, String> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Activity activity;
    ArrayList<Movie> movies;

    public GetMovieArr(Activity activity){
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }



    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG,"doInBackground() 호출");

        String serverURL="http://35.224.156.8:5000/movie_arr"; // 서버 url 주소

        try{
            Log.d(TAG,"serverURL: "+serverURL);
            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection(); //url을 연결한 HttpURLConnection 객체 생성
            httpURLConnection.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));
            String result = "";
            String line;
            while((line = br.readLine()) != null) {
                result = result + line + "\n";
            }
            return result;
        }catch (Exception e){
            Log.d(TAG,"Error "+e);
            return "서버접근안됨";
        }

    } // doInBackGround()

    @Override
    protected void onPostExecute(String response_arr) {
        super.onPostExecute(response_arr);

        movies = new ArrayList<>();
        Log.d(TAG,"response: "+response_arr);

        try{
            JSONArray response_json = new JSONArray(response_arr);
            for(int i=0; i<response_json.length();i++){
                JSONObject response = response_json.getJSONObject(i);
                Gson gson = new Gson();
                Movie movie = gson.fromJson(response.toString(),Movie.class);
//                Log.d(TAG,"movie.movie_code 수정 전: "+movie.movie_code);
                movie.setMovie_code(movie.movie_code.substring(movie.movie_code.length()-6));
//                Log.d(TAG,"movie.movie_code 수정 후: "+movie.movie_code);
                movies.add(movie);

            }
        }catch (JSONException e){
            Log.d(TAG,"JSONException: "+e.toString());
        }

        /*
         * 영화 recyclerview 관련 코드
         */
        RecyclerView movie_recyclerview = activity.findViewById(R.id.movie_recyclerview);
        movie_recyclerview.setLayoutManager(new GridLayoutManager(activity.getApplicationContext(),2));
        MovieAdapter movieAdapter = new MovieAdapter(movies,activity);
        movie_recyclerview.setAdapter(movieAdapter);

    }
} // 클래스
