package com.example.sentimentanalysis;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Review> reviews;
    Activity activity;
    Context context;

    /**
     * 생성자
     */
    public ReviewAdapter(ArrayList<Review> reviews, Activity activity){
        this.reviews = reviews;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // 생성자

    /**
     * onCreateViewHolder() 메소드
     */
    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_review_item,parent,false); // 뷰 객체 생성
        ReviewAdapter.ViewHolder viewHolder = new ReviewAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    /**
     * onBindViewHolder() 메소드
     */
    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!= RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건
            Review review = reviews.get(holder.getAdapterPosition());
            holder.review_content_tv.setText(review.review_content);

            if(Math.round(Float.parseFloat(review.review_analysis_percent))<60){ // 신뢰도가 60% 미만이라면
                holder.review_analysis_tv.setText("분석 불가");
                holder.review_analysis_percent_tv.setText("");
            }else{
                holder.review_analysis_tv.setText(review.review_analysis);
                holder.review_analysis_percent_tv.setText("신뢰도 : "+review.review_analysis_percent+"%");
            }


        }
    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     */
    @Override
    public int getItemCount() {
        return reviews.size();
    } // getItemCount() 메소드


    /**
     * 뷰홀더 클래스
     */
    class ViewHolder extends RecyclerView.ViewHolder{

        TextView review_content_tv;
        TextView review_analysis_tv;
        TextView review_analysis_percent_tv;

        /**
         * 뷰 홀더 생성자
         */
        ViewHolder(View item_view){
            super(item_view);
            review_content_tv = item_view.findViewById(R.id.review_content_tv);
            review_analysis_tv = item_view.findViewById(R.id.review_analysis_tv);
            review_analysis_percent_tv = item_view.findViewById(R.id.review_analysis_percent_tv);
        } // ViewHolder 생성자


    } // ViewHolder 클래스

} // TouristAdapter 클래스

