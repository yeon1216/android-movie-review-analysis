package com.example.sentimentanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    String movie_json; // 현재 영화 json
    Movie movie; // 현재 영화

    public GetReviewArr getReviewArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Log.d(TAG,"onCreate()");

        /*
         * intent로 여행지 정보 받아오기
         */
        Intent intent = getIntent();
        movie_json = intent.getStringExtra("movie_json");
        Gson gson = new Gson();
        movie = gson.fromJson(movie_json,Movie.class);
        ImageView movie_img_iv = findViewById(R.id.movie_img_iv);
        Glide.with(getApplicationContext())
                .load(movie.movie_img)
                .thumbnail(0.1f)
                .placeholder(R.drawable.loading3)
                .into(movie_img_iv);
        TextView movie_title_tv = findViewById(R.id.movie_title_tv);
        movie_title_tv.setText(movie.movie_title);

        getReviewArr = new GetReviewArr(ReviewActivity.this);
        getReviewArr.execute(movie.movie_code);

    } // onCreate() 메소드

} // ReviewActivity 클래스
