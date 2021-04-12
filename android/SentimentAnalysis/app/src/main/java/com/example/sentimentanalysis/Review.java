package com.example.sentimentanalysis;

public class Review {
    public String review_content;
    public String review_analysis;
    public String review_analysis_percent;

    public Review() {
    }

    public Review(String review_content, String review_analysis, String review_analysis_percent) {
        this.review_content = review_content;
        this.review_analysis = review_analysis;
        this.review_analysis_percent = review_analysis_percent;
    }

    public void setReview_content(String review_content) {
        this.review_content = review_content;
    }

    public void setReview_analysis(String review_analysis) {
        this.review_analysis = review_analysis;
    }

    public void setReview_analysis_percent(String review_analysis_percent) {
        this.review_analysis_percent = review_analysis_percent;
    }

    public String getReview_content() {
        return review_content;
    }

    public String getReview_analysis() {
        return review_analysis;
    }

    public String getReview_analysis_percent() {
        return review_analysis_percent;
    }
}
