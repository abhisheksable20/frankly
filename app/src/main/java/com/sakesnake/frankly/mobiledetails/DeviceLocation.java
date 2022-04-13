package com.sakesnake.frankly.mobiledetails;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// This class is not in use for now
public class DeviceLocation
{
    /*
        0 is for latitude
        1 is for longitude
    */
    private static ArrayList<Double> locationDetails = new ArrayList<>();

    public static String getDeviceLocation(Context context)
    {
        FusedLocationProviderClient mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationProvider.getLastLocation().addOnCompleteListener(task ->
        {
            Location location = task.getResult();
            if (location != null) {
                locationDetails.add(location.getLatitude());
                locationDetails.add(location.getLongitude());
            }
            else {
                // getting new location data
                LocationRequest newLocationRequest = new LocationRequest();
                newLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                newLocationRequest.setInterval(5);
                newLocationRequest.setFastestInterval(0);
                newLocationRequest.setNumUpdates(1);
                mFusedLocationProvider.requestLocationUpdates(newLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult)
                    {
                        super.onLocationResult(locationResult);
                        Location latestLocation = locationResult.getLastLocation();
                        locationDetails.add(latestLocation.getLatitude());
                        locationDetails.add(latestLocation.getLongitude());
                    }
                }, Looper.myLooper());
            }
        });

        return locationGeoCoding(context,locationDetails);
    }

    public static String locationGeoCoding(Context context,ArrayList<Double> locationCoordinates)
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocation(locationCoordinates.get(0),locationCoordinates.get(1),1);
            Address address = addresses.get(0);
            if (address.getCountryName() == null  &&  address.getLocality() != null)
            {
                return address.getLocality();
            }
            else if (address.getLocality() == null  &&  address.getCountryName() != null)
            {
                return address.getCountryName();
            }
            else if (address.getCountryName() != null  &&  address.getLocality() != null)
            {
                return address.getLocality()+" . "+address.getCountryName();
            }
            else
            {
                return "Unknown";
            }
        }
        catch (IOException e)
        {
            return "Unknown";
        }
    }
}
