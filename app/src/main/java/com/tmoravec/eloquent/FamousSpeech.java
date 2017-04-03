package com.tmoravec.eloquent;

/*
 * Basically a c-like struct.
 */
public class FamousSpeech {
    public String mTitle;
    public String mAuthor;
    public String mContent;

    public FamousSpeech() {

    }

    public FamousSpeech(String title, String author, String content) {
        reset(title, author, content);
    }

    public void reset(String title, String author, String content) {
        mTitle = title;
        mAuthor = author;
        mContent = content;
    }
}
