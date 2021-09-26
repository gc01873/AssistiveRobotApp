package com.example.bluetooth2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class GPSService {


    //this class will hold the gps objects that will be accessed by the other parts of the program
    //this will be a singleton class
    private static final int PERMISSIONS_FINE_LOCATION = 100;
    private static GPSService GPS;
    public Activity activity;
    // public Activity activity = new GPSActivity();
    String TAG = "GPS Service";
    private static int DEFAULT_UPDATE_INTERVAL = 10;
    private static int FASTEST_UPDATE_INTERVAL = 5;
    private Boolean requestingLocationUpdates = false;

    //current location
    Location currentLocation;
    Location mCurrentLocation;

    //list of saved locations

    List<Location> savedLocations;
    //Very important
    LocationRequest locationRequest;
    LocationCallback locationCallBack;


    //the majority of gps will depend on this. The Google API for location services
    private FusedLocationProviderClient fusedLocationProviderClient;

    ///make the activity a parameter in the constructor
    public GPSService(Activity activity) {
        //  if(GPS==null){

        // }

        //instantiate the declared objects
        this.activity = activity;
        locationRequest = new LocationRequest();
        locationRequest = LocationRequest.create();
        //may switch to main
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);


//Create a locationCallBack object
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                Location location = locationResult.getLastLocation();
                //updateUIValues(location);

            }
        };


    }

    //Sets the location request parameters
    public void setLocationRequests(long interval, long fastestUpdateInterval, int priority) {
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestUpdateInterval);
        locationRequest.setPriority(priority);

    }


    //returns the last location

    public Location getLastLocation() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mCurrentLocation = location;
                        }
                    }
                });
        return mCurrentLocation;
    }

//This method will provide updates to the location once called
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(!requestingLocationUpdates) {
            //Should turn false to true
            requestingLocationUpdates = !requestingLocationUpdates;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallBack,
                    Looper.getMainLooper());
        }
    }

//This will stop the location updates from the app
    public void stopLocationUpdates() {
        if(requestingLocationUpdates)
            //should turn true to false
            requestingLocationUpdates = !requestingLocationUpdates;
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);

    }













    private void updateGPS() {
        //getPermission from the user to track GPS
        //this may need to change depending on the class we are in
        fusedLocationProviderClient = new FusedLocationProviderClient(activity);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //We got permissions. Put the values of location. XXX into the UI Components.
                    //updateUIValues(location);
                    //currentLocation = location;
                    System.out.println("Phone Longitude: " + location.getLongitude());
                    System.out.println("Phone Latitude: " + location.getLatitude());
                    Log.d(TAG, "Phone Longitude: " + location.getLongitude());
                    Log.d(TAG, "Phone Latitude: " + location.getLatitude());
                    Log.d(TAG, "Phone altitude: " + location.getAltitude());
                    Log.d(TAG, "Accuracy: " + location.getAccuracy());


                }
            });

        } else {
            //if permissions not granted

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);

            }
        }


    }








}
