package com.example.sentimentanalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

/**
 * 댓글 등록 화면 클래스
 */
public class RegisterReviewActivity extends AppCompatActivity {
    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_review);
        Log.d(TAG,"onCreate() 메소드");
    } // onCreate() 메소드
} // 댓글 등록 화면 클래스
