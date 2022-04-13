package com.sakesnake.frankly.mobiledetails;

import android.os.Build;

public class DeviceName
{
    public static String getDeviceName()
    {
        try
        {
            return perfectManufacturerName(Build.MANUFACTURER)+" "+Build.MODEL;
        }
        catch (NullPointerException e)
        {
            return "Unknown";
        }
    }

    public static String perfectManufacturerName(String manufacturer)
    {
        String firstLetter = manufacturer.substring(0,1);
        String remainingLetter = manufacturer.substring(1);
        firstLetter = firstLetter.toUpperCase();
        return firstLetter+remainingLetter;
    }
}

