package com.example.sentimentanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button search_movie_btn = findViewById(R.id.search_movie_btn); // 영화 검색 버튼
        Button register_review_btn = findViewById(R.id.register_review_btn); // 리뷰 등록 버튼

        search_movie_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 영화 검색 버튼 클릭시 이벤트
                startActivity(new Intent(getApplicationContext(),SearchMovieActivity.class));
            }
        });

        register_review_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 리뷰 등록 버튼 클릭시 이벤트
                startActivity(new Intent(getApplicationContext(),RegisterReviewActivity.class));
            }
        });

    } // onCreate() 메소드

} // MainActivity 클래스
