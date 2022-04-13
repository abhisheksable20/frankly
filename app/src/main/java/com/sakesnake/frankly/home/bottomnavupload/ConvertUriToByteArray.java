package com.sakesnake.frankly.home.bottomnavupload;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.sakesnake.frankly.App;
import com.sakesnake.frankly.home.ImageResizer;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConvertUriToByteArray {

    // converting list of uri into byteArray list
    public static void getByteArrayFromImagesList(List<Uri> imageUri, UriListConvertCallback callback,Context appContext) {
        List<byte[]> byteImages = new ArrayList<>();
        App.getCachedExecutorService().execute(() -> {
            for (int i = 0; i < imageUri.size(); i++) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Bitmap bitmap = ImageResizer.reduceBitmapSize(imageUri.get(i), 307200,appContext);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byteImages.add(outputStream.toByteArray());
            }

            // Converting uri to byte done callback
            App.getMainThread().post(() -> {
                if (callback != null)
                    callback.getByteArrayImages(byteImages);
            });
        });
    }


    // converting single image into byte array
    public static void getByteArrayFromImage(Uri songImage, UriConvertCallback callback,Context appContext) {
        App.getCachedExecutorService().execute(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Bitmap bitmap = ImageResizer.reduceBitmapSize(songImage, 307200,appContext);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            App.getMainThread().post(() -> {
                callback.getByteArrayFromImage(outputStream.toByteArray());
            });
        });
    }
}
