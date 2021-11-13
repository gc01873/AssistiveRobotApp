package com.example.bluetooth2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class GPSActivity extends AppCompatActivity {
    private static final int PERMISSIONS_FINE_LOCATION = 100;
    //The textviews and switches for gps page
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_address, tv_updates,tv_waypointCounts;
    Switch sw_locationupdates, sw_gps;
    Button btn_newWayPoint, btn_showWayPoint;

    GPSService GPS; 

    private static int DEFAULT_UPDATE_INTERVAL = 10;
    private static int FASTEST_UPDATE_INTERVAL = 5;

    //Variable to remember if we are tracking location or not.
    boolean updateOn= false;

    //current location
    Location currentLocation;

    //list of saved locations

    List<Location>savedLocations;



    //Very important
    LocationRequest locationRequest;
    LocationCallback locationCallBack;



    //the majority of gps will depend on this. The Google API for location services
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        //Give each UI variable a reference
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_address = findViewById(R.id.tv_address);
        tv_updates = findViewById(R.id.tv_updates);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);
        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPoint = findViewById(R.id.btn_showWayPoint);
        tv_waypointCounts = findViewById(R.id.tv_countOfCrumbs);
        GPS = new GPSService(this);


        //here we will declare and begin using actual location services

        locationRequest = new LocationRequest();
        //The interval which it will send a request for the phone's location.
        //This is in miliseconds
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);

        //when the Location request is optimized
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//event that is triggered whenever there is a a request for update interval
        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);

            }
        };

        btn_newWayPoint.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //get the gps location
MyGPSApplication MyApplication = (MyGPSApplication)getApplicationContext();
savedLocations= MyApplication.getMyLocations();
savedLocations.add(currentLocation);
                //add the new location to the global list

            }
        });


        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    //For most accuracy, use gps (USES ALOT OF POWER)
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS Sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }

            }
        });


        sw_locationupdates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    //Starts location updates
                    startLocationUpdates();
                } else {
                    //Stops location updates
                    stopLocationUpdates();
                }
            }
        });
        updateGPS();
    } //end of the onCreate method

    private void startLocationUpdates() {
        tv_updates.setText("Location is being updated..");
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location updating has stopped.");
        tv_lat.setText("Not Being tracked");
        tv_lon.setText("Not Being Tracked");
        tv_accuracy.setText("Not Being Tracked");
        tv_speed.setText("Not Being Tracked");
        tv_address.setText("Not Being Tracked");
        tv_sensor.setText("No longer being tracked");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this,"This app requires permission in order to work properly",Toast.LENGTH_SHORT).show();
                    finish();

                }
                break;
        }

    }

    private void updateGPS(){
        //getPermission from the user to track GPS
        //this may need to change depending on the class we are in
        fusedLocationProviderClient = new FusedLocationProviderClient(GPSActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //We got permissions. Put the values of location. XXX into the UI Components.
                    updateUIValues(location);
                    currentLocation = location;


                }
            });

        }
        else {
            //if permissions not granted

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_FINE_LOCATION);

            }
        }

        //get the current location from the fused client
        //update the UI - i.e. set all propeties in their associated textview items.
    }

    private void updateUIValues(Location location){
        //update all of the text view objects with a new location.
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else
        {
            tv_altitude.setText("Not Available");
        }

        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }

        else
        {
            tv_speed.setText("Not Available");
        }
        Geocoder geocoder = new Geocoder(this);
        try{
            List<Address> address = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(address.get(0).getAddressLine(0));


        }
        catch(Exception e){
            tv_address.setText("Unable to get Street address");

        }
        MyGPSApplication MyApplication = (MyGPSApplication)getApplicationContext();
        savedLocations= MyApplication.getMyLocations();
        tv_waypointCounts.setText((Integer.toString(savedLocations.size())));

    }

}