package com.sakesnake.frankly.home.postfeeds;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.List;

public class SharePost {
    // Sharing post with other apps
    // smallTextOne - tile
    // smallTextTwo - author
    // mainText - main content
    public static void sharePost(final String smallTextOne,final String smallTextTwo,final String mainText,final List<String> imagesUri,@NonNull final Context mContext){
        final StringBuilder stringBuilder = new StringBuilder();
        if (smallTextOne != null)
            stringBuilder.append(smallTextOne).append("\n");
        if (smallTextTwo != null)
            stringBuilder.append(smallTextTwo).append("\n");
        if (mainText != null)
            stringBuilder.append(mainText).append("\n");

        if (imagesUri != null && imagesUri.size() > 0){
            for (String url:imagesUri) {
                stringBuilder.append(url).append("\n");
            }
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,stringBuilder.toString());
        intent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(intent,"Share Post Frankly");
        mContext.startActivity(shareIntent);
    }
}
