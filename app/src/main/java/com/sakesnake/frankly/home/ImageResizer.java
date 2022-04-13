package com.sakesnake.frankly.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.sakesnake.frankly.Constants;

import java.io.IOException;

public class ImageResizer
{
    // Recommended MAX_SIZE 307200
    public static Bitmap reduceBitmapSize(Uri bitmapData, int MAX_SIZE,Context appContext)
    {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(appContext.getContentResolver(),bitmapData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double ratioSquare;
        int bitmapHeight, bitmapWidth;
        bitmapHeight = bitmap.getHeight();
        bitmapWidth = bitmap.getWidth();
        ratioSquare = (bitmapHeight * bitmapWidth) / MAX_SIZE;
        if (ratioSquare <= 1)
            return bitmap;
        double ratio = Math.sqrt(ratioSquare);
        Log.d(Constants.TAG, "Ratio: " + ratio);
        int requiredHeight = (int) Math.round(bitmapHeight / ratio);
        int requiredWidth = (int) Math.round(bitmapWidth / ratio);
        Bitmap compressedBitmap = Bitmap.createScaledBitmap(bitmap, requiredWidth, requiredHeight, true);

        int rotation = getImageRotation(bitmapData.getPath());

        if (rotation != 0)
        {
            compressedBitmap = getRotatedBitmap(compressedBitmap,rotation);
        }
        return compressedBitmap;
    }

    public static int getImageRotation(String filePath)
    {
        ExifInterface exifInterface = null;
        int exifRotation = 0;

        try
        {
           exifInterface = new ExifInterface(filePath);
           exifRotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (exifInterface == null)
            return 0;
        else
            return exifToDegree(exifRotation);

    }

    private static int exifToDegree(int rotation)
    {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            return 90;
        }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }

        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        else
        {
            return 0;
        }

    }


    private static Bitmap getRotatedBitmap(Bitmap bitmap,int rotationDegree)
    {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }


}


/*
//this is the byte stream that I upload.
public static byte[] getStreamByteFromImage(final File imageFile)
{

    Bitmap photoBitmap = BitmapFactory.decodeFile(imageFile.getPath());
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    int imageRotation = getImageRotation(imageFile);

    if (imageRotation != 0)
        photoBitmap = getBitmapRotatedByDegree(photoBitmap, imageRotation);

    photoBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

    return stream.toByteArray();
}



private static int getImageRotation(final File imageFile) {

    ExifInterface exif = null;
    int exifRotation = 0;

    try {
        exif = new ExifInterface(imageFile.getPath());
        exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    } catch (IOException e) {
        e.printStackTrace();
    }

    if (exif == null)
        return 0;
    else
        return exifToDegrees(exifRotation);
}

private static int exifToDegrees(int rotation) {
    if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
        return 90;
    else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
        return 180;
    else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
        return 270;

    return 0;
}

private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
    Matrix matrix = new Matrix();
    matrix.preRotate(rotationDegree);

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
}
 **/
