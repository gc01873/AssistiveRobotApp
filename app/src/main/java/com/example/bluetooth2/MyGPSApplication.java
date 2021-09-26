package com.example.bluetooth2;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class MyGPSApplication extends Application {

    private static MyGPSApplication singleton;
    private List<Location> myLocations;


    public MyGPSApplication getInstance(){
        return singleton;
    }

    public void onCreate(){
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<Location>();
        //myLocations = new ArrayList<>();
    }

    public List<Location> getMyLocations() {
        return myLocations;
    }
}
