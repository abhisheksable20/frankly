package com.sakesnake.frankly.home.bottomnavupload.imageupload;

import android.net.Uri;

public class MobileImages
{
    private Uri deviceImageUri;

    public MobileImages(Uri deviceImageUri)
    {
        this.deviceImageUri = deviceImageUri;
    }


    public Uri getDeviceImageUri()
    {
        return deviceImageUri;
    }
}
