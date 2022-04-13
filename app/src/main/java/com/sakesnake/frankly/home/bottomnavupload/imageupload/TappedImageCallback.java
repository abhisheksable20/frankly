package com.sakesnake.frankly.home.bottomnavupload.imageupload;

import android.net.Uri;

import java.util.List;

public interface TappedImageCallback {
    void tappedImage(int position, List<Uri> preSelectedImages);
}
