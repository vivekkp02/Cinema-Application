package com.android.example.cinemaapp.app;

/**
 * Created by V on 4/15/2016.
 */
public class Reviews {

    private String author;
    private String reviewContent;

    public Reviews(String author, String reviewContent) {
        this.author = author;
        this.reviewContent = reviewContent;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }
}

