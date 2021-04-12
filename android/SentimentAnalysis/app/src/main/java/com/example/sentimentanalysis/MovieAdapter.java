package com.example.sentimentanalysis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Movie> movies;
    Activity activity;
    Context context;

    /**
     * 생성자
     */
    public MovieAdapter(ArrayList<Movie> movies, Activity activity){
        this.movies = movies;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // 생성자

    /**
     * onCreateViewHolder() 메소드
     */
    @NonNull
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_movie_item,parent,false); // 뷰 객체 생성
        MovieAdapter.ViewHolder viewHolder = new MovieAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    /**
     * onBindViewHolder() 메소드
     */
    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.ViewHolder holder, int position) {
        if(holder.getAdapterPosition()!= RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건
            final Movie movie = movies.get(holder.getAdapterPosition());

            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // 아이템 클릭시 이벤트
                    Gson gson = new Gson();
                    String movie_str = gson.toJson(movie);
                    Intent intent = new Intent(context, ReviewActivity.class);
                    intent.putExtra("movie_json",movie_str);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });

            /*
             * 이미지 적용
             */
            Glide.with(context)
                    .load(movie.movie_img)
                    .thumbnail(0.1f)
                    .placeholder(R.drawable.loading3)
                    .into(holder.movie_img_iv);

            /*
             * 영화 title
             */
            holder.movie_title_tv.setText(movie.movie_title);

        }
    } // onBindViewHolder() 메소드

    /**
     * getItemCount() 메소드
     */
    @Override
    public int getItemCount() {
        return movies.size();
    } // getItemCount() 메소드


    /**
     * 뷰홀더 클래스
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        View item_view;
        ImageView movie_img_iv;
        TextView movie_title_tv;

        /**
         * 뷰 홀더 생성자
         */
        ViewHolder(View item_view){
            super(item_view);
            this.item_view = item_view;
            movie_img_iv = item_view.findViewById(R.id.movie_img_iv);
            movie_title_tv = item_view.findViewById(R.id.movie_title_tv);

            /**
             * 아이템 클릭시 이벤트
             */
            item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        } // ViewHolder 생성자


    } // ViewHolder 클래스

} // TouristAdapter 클래스
