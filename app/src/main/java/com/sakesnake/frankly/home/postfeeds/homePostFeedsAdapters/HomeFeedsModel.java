package com.sakesnake.frankly.home.postfeeds.homePostFeedsAdapters;

import com.parse.ParseObject;

import java.util.List;

// Home feed model applied to every content type
public class HomeFeedsModel {

    private String feedTitle;

    private List<ParseObject> feedData;

    private int contentType;

    public HomeFeedsModel(String feedTitle,int contentType ,List<ParseObject> feedData) {
        this.feedTitle = feedTitle;
        this.feedData = feedData;
        this.contentType = contentType;
    }

    public List<ParseObject> getFeedData() {
        return feedData;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public int getContentType(){
        return contentType;
    }
}
