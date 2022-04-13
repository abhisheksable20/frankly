package com.sakesnake.frankly.home.bottomnavupload.multiimageselector;

import android.net.Uri;

import java.util.List;

public interface PreImageSelectionCallback {
    void selectedImages(List<Uri> selectedImages);
}
