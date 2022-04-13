package com.sakesnake.frankly;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class IsInternetAvailable
{
    public static boolean isInternetAvailable(Context context)
    {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
