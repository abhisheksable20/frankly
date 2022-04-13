package com.sakesnake.frankly.home.bottomnavhome.nofeeds;

import android.graphics.drawable.Drawable;

public class NoFeeds {

    private Drawable contentImage;

    private String contentName,contentMessage;

    private int contentType;

    public NoFeeds(Drawable contentImage, String contentName, String contentMessage, int contentType) {
        this.contentImage = contentImage;
        this.contentName = contentName;
        this.contentMessage = contentMessage;
        this.contentType = contentType;
    }

    public Drawable getContentImage() {
        return contentImage;
    }

    public String getContentName() {
        return contentName;
    }

    public String getContentMessage() {
        return contentMessage;
    }

    public int getContentType() {
        return contentType;
    }
}
