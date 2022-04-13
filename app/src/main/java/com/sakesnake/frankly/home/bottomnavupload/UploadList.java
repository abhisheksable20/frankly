package com.sakesnake.frankly.home.bottomnavupload;

public class UploadList
{
    private String uploadName;

    private int uploadConstraintLayoutBackground;

    private int contentType;

    public UploadList(String uploadName, int uploadConstraintLayoutBackground, int contentType)
    {
        this.uploadName = uploadName;
        this.uploadConstraintLayoutBackground = uploadConstraintLayoutBackground;
        this.contentType = contentType;
    }

    public String getUploadName()
    {
        return uploadName;
    }

    public int getUploadCardBackground()
    {
        return uploadConstraintLayoutBackground;
    }

    public int getContentType() {
        return contentType;
    }
}
