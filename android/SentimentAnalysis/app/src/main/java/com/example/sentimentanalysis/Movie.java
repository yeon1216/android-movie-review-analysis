package com.example.sentimentanalysis;

public class Movie {

    public String movie_title;
    public String movie_img;
    public String movie_code;

    public Movie() {
    }

    public Movie(String movie_title, String movie_img, String movie_code) {
        this.movie_title = movie_title;
        this.movie_img = movie_img;
        this.movie_code = movie_code;
    }

    public void setMovie_code(String movie_code) {
        this.movie_code = movie_code;
    }

    public String getMovie_code() {
        return movie_code;
    }

    public void setMovie_title(String movie_title) {
        this.movie_title = movie_title;
    }

    public void setMovie_img(String movie_img) {
        this.movie_img = movie_img;
    }

    public String getMovie_title() {
        return movie_title;
    }

    public String getMovie_img() {
        return movie_img;
    }
}
