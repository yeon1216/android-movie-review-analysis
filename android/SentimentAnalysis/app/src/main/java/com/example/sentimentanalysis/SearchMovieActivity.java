package com.example.sentimentanalysis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 영화검색 activity 클래스
 */
public class SearchMovieActivity extends AppCompatActivity {
    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    RecyclerView movie_recyclerview;
    ArrayList<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);
        Log.d(TAG,"onCreate() 메소드");

//        getMovieArr();
        GetMovieArr getMovieArr = new GetMovieArr(SearchMovieActivity.this);
        getMovieArr.execute();

        movie_recyclerview = findViewById(R.id.movie_recyclerview); // 영화 리싸이클러뷰

    } // onCreate() 메소드

    /**
     * 네이버 영화를 크롤링하는 메소드
     */
    public void getMovieArr(){
        Log.d(TAG,"getMovieArr() 호출");

        movies = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
//        params.put("mode","get_broadcast_list");

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, "http://35.224.156.8:5000/movie_arr", jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response_arr) { // 응답 성공
                        Log.d(TAG,"response_arr: "+response_arr.toString());
                        try{
                            for(int i=0; i<response_arr.length();i++){
                                JSONObject response = response_arr.getJSONObject(i);
                                Gson gson = new Gson();
                                Movie movie = gson.fromJson(response.toString(),Movie.class);
                                movies.add(movie);
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /*
                         * 영화 recyclerview 관련 코드
                         */
                        movie_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        MovieAdapter movieAdapter = new MovieAdapter(movies,SearchMovieActivity.this);
//                        sort(broadcastRooms); // 방송을 최근순으로 정렬
                        movie_recyclerview.setAdapter(movieAdapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        });

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonArrayRequest); // 요청 큐에 위 요청 추가
    }
} // 영화검색 activity 클래스
