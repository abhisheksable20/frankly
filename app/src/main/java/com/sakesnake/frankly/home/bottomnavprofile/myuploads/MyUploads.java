package com.sakesnake.frankly.home.bottomnavprofile.myuploads;

public class MyUploads {
    private String myUploadName;

    private int myUploadConstraintLayoutBackground;

    private int contentType;

    public MyUploads(String myUploadName, int myUploadConstraintLayoutBackground, int contentType) {
        this.myUploadName = myUploadName;
        this.myUploadConstraintLayoutBackground = myUploadConstraintLayoutBackground;
        this.contentType = contentType;
    }

    public String getMyUploadName() {
        return myUploadName;
    }

    public int getUploadConstraintBackground() {
        return myUploadConstraintLayoutBackground;
    }

    public int getContentType() {
        return contentType;
    }
}
