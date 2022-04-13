package com.sakesnake.frankly.parsedatabase;

import android.content.Context;

import com.sakesnake.frankly.mobiledetails.DeviceLocation;
import com.sakesnake.frankly.mobiledetails.DeviceName;
import com.parse.ParseSession;

public class ParseSessions {
    public static void saveDeviceName() {
        ParseSession.getCurrentSessionInBackground((object, e) ->
        {
            if (e == null) {
                object.put("deviceName", DeviceName.getDeviceName());
                object.saveInBackground();
            }
        });
    }

    // Method not in use
    public static void saveDeviceLocation(Context mContext) {
        ParseSession.getCurrentSessionInBackground((object, e) ->
        {
            if (e == null) {
                object.put("deviceLocation", DeviceLocation.getDeviceLocation(mContext));
                object.saveInBackground();
            }
        });
    }
}
