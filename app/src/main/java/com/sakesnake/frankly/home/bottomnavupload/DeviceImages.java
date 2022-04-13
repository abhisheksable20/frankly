package com.sakesnake.frankly.home.bottomnavupload;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.sakesnake.frankly.App;
import com.sakesnake.frankly.home.bottomnavupload.imageupload.MobileImages;
import com.sakesnake.frankly.home.bottomnavupload.multiimageselector.DeviceImagesCallback;

import java.util.ArrayList;
import java.util.List;

public class DeviceImages {
    @Deprecated
    public static ArrayList<MobileImages> getDeviceImages(Context mContext) {
        ArrayList<MobileImages> mobileImages = new ArrayList<>();
        Uri mediaStoreUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] mProjection = {MediaStore.Images.ImageColumns._ID};
        String mSelection = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";
        Cursor cursor = mContext.getContentResolver().query(mediaStoreUri, mProjection, null, null, mSelection);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Uri mediaUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                mobileImages.add(new MobileImages(mediaUri));
            }
            cursor.close();
            if (mobileImages.size() <= 0) {
                return null;
            } else {
                return mobileImages;
            }
        } else {
            return null;
        }
    }

    public static void getMobileImages(Context appContext, DeviceImagesCallback callback) {
        App.getCachedExecutorService().execute(() -> {
            final List<Uri> imageUris = new ArrayList<>();
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            else
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] select = {MediaStore.Images.Media._ID,MediaStore.Images.Media.MIME_TYPE};
                String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
                Cursor cursor = appContext.getContentResolver().query(uri, select, null, null, sortOrder);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String mimeType = cursor.getString(1);
                        if (mimeType != null){
                            if (mimeType.equalsIgnoreCase("image/jpeg") || mimeType.equals("image/png") || mimeType.equals("image/jpg")){
                                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                                imageUris.add(imageUri);
                            }
                        }
                }
                    cursor.close();
                }

            App.getMainThread().post(() -> {
                if (callback != null) {
                    callback.deviceImages(imageUris);
                }
            });
        });
    }
}
