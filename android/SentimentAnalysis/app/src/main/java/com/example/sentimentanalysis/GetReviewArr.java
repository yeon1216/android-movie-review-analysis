package com.example.sentimentanalysis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetReviewArr extends AsyncTask<String, Void, String> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그


    ReviewActivity activity;
    ArrayList<Review> reviews;

    ProgressDialog progressDialog; // 프로그래스 다이얼로그

    String movie_code;

    public GetReviewArr(ReviewActivity activity){
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loading(); // 프로그래스 다이얼로그 실행
            }
        });

        movie_code = strings[0];
        String serverURL="http://35.224.156.8:5000/review_arr2?movie_code="+movie_code; // 서버 url 주소

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
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    loadingEnd(); // 프로그래스 다이얼로그 종료
                    showFailGetReview();
                }
            });

            return "서버접근안됨";
        }

    } // doInBackGround()

    @Override
    protected void onPostExecute(String response_arr) {
        super.onPostExecute(response_arr);
        Log.d(TAG,"onPostExecute() 호출");

        reviews = new ArrayList<>();
        Log.d(TAG,"response: "+response_arr);
        if("no_review".equals(response_arr)){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity.getApplicationContext(),"분석할 리뷰가 없습니다",Toast.LENGTH_LONG).show();
                    loadingEnd();
                }
            });
            return;
        }
        try{
            JSONArray response_json = new JSONArray(response_arr);
            for(int i=0; i<response_json.length();i++){
                JSONObject response = response_json.getJSONObject(i);
                Gson gson = new Gson();
                Review review = gson.fromJson(response.toString(),Review.class);
//                Log.d(TAG,"리뷰 내용: "+review.review_content+", 리뷰 분석: "+review.review_analysis+", 신뢰도: "+review.review_analysis_percent);
                reviews.add(review);
            }
        }catch (JSONException e){
            Log.d(TAG,"JSONException: "+e.toString());
        }

        int positive_review_count = 0;
        int negative_review_count = 0;
        int none_review_count = 0;

        for (int i = 0; i < reviews.size(); i++) {
            if(Math.round(Float.parseFloat(reviews.get(i).review_analysis_percent))<60){ // 신뢰도가 60% 미만이라면
                none_review_count++;
            }else{
                if("긍정".equals(reviews.get(i).review_analysis)){
                    positive_review_count++;
                }else if("부정".equals(reviews.get(i).review_analysis)){
                    negative_review_count++;
                }
            }
        }

        TextView positive_review_count_tv = activity.findViewById(R.id.positive_review_count_tv);
        TextView negative_review_count_tv = activity.findViewById(R.id.negative_review_count_tv);
        TextView none_review_count_tv = activity.findViewById(R.id.none_review_count_tv);

        positive_review_count_tv.setText("긍정적인 리뷰 : "+positive_review_count+"개");
        negative_review_count_tv.setText("부정적인 리뷰 : "+negative_review_count+"개");
        none_review_count_tv.setText("분석하기 애매한 리뷰 : "+none_review_count+"개");

        /*
         * 영화 recyclerview 관련 코드
         */
        RecyclerView review_recyclerview = activity.findViewById(R.id.review_recyclerview);
        review_recyclerview.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext()));
        ReviewAdapter reviewAdapter = new ReviewAdapter(reviews,activity);
        review_recyclerview.setAdapter(reviewAdapter);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loadingEnd();
            }
        });

    } // onPostExecute() 메소드

    /**
     * 로딩 메소드
     */
    public void loading() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("잠시만 기다려 주세요");
                        progressDialog.show();
                    }
                }, 0);
    }

    /**
     * 로딩종료 메소드
     */
    public void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 0);
    }

    /**
     * 리뷰 가져오기 실패 다이얼로그
     */
    private void showFailGetReview(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Movie Review Analysis");
        builder.setMessage("리뷰데이터를 가지고오지 못했습니다.");
        builder.setNegativeButton("다시시도",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.getReviewArr = null;
                        activity.getReviewArr = new GetReviewArr(activity);
                        activity.getReviewArr.execute(movie_code);
                    }
                });
        builder.setPositiveButton("나가기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish(); // activity 종료
                    }
                });
        builder.show(); // 다이얼로그 보이게 하기
    }
} // 클래스


