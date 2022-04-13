package com.sakesnake.frankly.home.bottomnavsearch;

public class SearchList {
    private String searchName;

    private int constraintLayoutColor;

    private int contentType;

    public SearchList(String searchName, int constraintLayoutColor, int contentType) {
        this.searchName = searchName;
        this.constraintLayoutColor = constraintLayoutColor;
        this.contentType = contentType;
    }

    public String getSearchName() {
        return searchName;
    }

    public int getConstraintLayoutColor() {
        return constraintLayoutColor;
    }

    public int getContentType() {
        return contentType;
    }
}
