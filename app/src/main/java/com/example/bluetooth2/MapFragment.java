package com.example.bluetooth2;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


/* This fragment will display a map view which will have the user's location, the location of the raspberry pi
and the option to add a point to the bot for it to go to that destination
 */
public class MapFragment extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private BluetoothConnectActivity mBActivity;

    //Buttons being added to followMe fragment
    Button send;
    Button followMe;
    Button ControlMe;
    Button demo;
    Button demo1;
    Button demo2;
    Button demo3;
    Boolean demo1Clicked = false;
    Boolean demo2Clicked = false;
    Boolean demo3Clicked = false;



    EditText write;
    TextView MessageLog;

    Thread GPS_t;

    FragmentManager fm;



    GPSService mapGps;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mBActivity = (BluetoothConnectActivity) getActivity();
        followMe = (Button) view.findViewById(R.id.FollowMe);
        ControlMe = (Button) view.findViewById(R.id.ControlMe);
        demo = (Button) view.findViewById(R.id.Map);
        demo1 = (Button) view.findViewById(R.id.demo1);
        demo2 = (Button) view.findViewById(R.id.demo2);
        demo3 = (Button) view.findViewById(R.id.demo3);


//This will manage the various fragments
        fm = mBActivity.getSupportFragmentManager();

//This will make the map button unclickable
        demo.setAlpha(.5f);
        demo.setClickable(false);
        //This should give us the gps from the activity
        if(mBActivity.getGPS()!=null){
            mapGps = mBActivity.getGPS();
            Log.d(mBActivity.getTAG(),"GPS Service is not null!");
        }
        else{
            Log.e(mBActivity.getTAG(),"GPS Service seems to be null!");
        }
        Location currentL = mapGps.getLastLocation();
        String demoData;
        demoData = "demoMe," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + "demo" + "\n";
        byte[] bytes = new byte[0];
        try {
            bytes = demoData.getBytes("UTF-8");
            mBActivity.mBluetoothConnection.write(bytes);
            Log.d(mBActivity.getTAG(), "Message written to Pi: " + demoData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }







        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBActivity.getFollowMeFragment() == null) {
                    mBActivity.setFollowMeFragment(new FollowMeFragment());
                    Log.d(mBActivity.getTAG(), "A New FollowME Fragment has been created");
                    fm.beginTransaction().replace(R.id.fragment_container_view, mBActivity.getFollowMeFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Log.d(mBActivity.getTAG(), " FollowMe Fragment of the Bluetooth Activity has been created");
                    fm.beginTransaction().replace(R.id.fragment_container_view, mBActivity.getFollowMeFragment())
                            .addToBackStack(null)
                            .commit();


                }
            }

        });
        //Map me will call t
        demo.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         if (mBActivity.getMapFragment() == null) {
                                             mBActivity.setMapFragment(new MapFragment());
                                             Log.d(mBActivity.getTAG(), "A New Map Fragment has been created");
                                             fm.beginTransaction().replace(R.id.fragment_container_view, mBActivity.getMapFragment())
                                                     .addToBackStack(null)
                                                     .commit();
                                         } else {
                                             Log.d(mBActivity.getTAG(), " Map Fragment of the Bluetooth Activity has been created");
                                             fm.beginTransaction().replace(R.id.fragment_container_view, mBActivity.getMapFragment())
                                                     .addToBackStack(null)
                                                     .commit();
                                         }
                                     }
                                 }

        );

        ControlMe.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             if (mBActivity.getControlMeFragment() == null) {
                                                 mBActivity.setControlMeFragment(new ControlMeFragment());
                                                 Log.d(mBActivity.getTAG(), "A New ControlMe Fragment has been created");
                                                 fm.beginTransaction().replace(R.id.fragment_container_view, mBActivity.getControlMeFragment())
                                                         .addToBackStack(null)
                                                         .commit();
                                             } else {
                                                 Log.d(mBActivity.getTAG(), " ControlMe fragment of the Bluetooth Activity has been created");
                                                 fm.beginTransaction().replace(R.id.fragment_container_view, mBActivity.getControlMeFragment())
                                                         .addToBackStack(null)
                                                         .commit();
                                             }
                                         }
                                     }

        );

        demo1.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             demo1.setAlpha(.5f);
                                             demo1.setClickable(false);
                                             String demoData;
                                             demoData = "demoMe," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + "demo1" + "\n";
                                             byte[] bytes = new byte[0];
                                             try {
                                                 bytes = demoData.getBytes("UTF-8");
                                                 mBActivity.mBluetoothConnection.write(bytes);
                                                 Log.d(mBActivity.getTAG(), "Message written to Pi: " + demoData);
                                             } catch (UnsupportedEncodingException e) {
                                                 e.printStackTrace();
                                             }
                                             while(true){
                                                 String pimsg = mBActivity.mBluetoothConnection.getMessage();
                                                 if(pimsg.equals("done!")){
                                                     demo1.setAlpha(1f);
                                                     demo1.setClickable(true);
                                                     break;
                                                 }
                                             }




                                             }
                                         }



        );


        demo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                demo2.setAlpha(.5f);
                demo2.setClickable(false);
                String demoData;
                demoData = "demoMe," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + "demo2" + "\n";
                byte[] bytes = new byte[0];
                try {
                    bytes = demoData.getBytes("UTF-8");
                    mBActivity.mBluetoothConnection.write(bytes);
                    Log.d(mBActivity.getTAG(), "Message written to Pi: " + demoData);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                while(true){
                    String pimsg = mBActivity.mBluetoothConnection.getMessage();
                    if(pimsg.equals("done2")){
                        demo2.setAlpha(1f);
                        demo2.setClickable(true);
                        break;
                    }

                }




            }
        });


        demo3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                demo3.setAlpha(.5f);
                demo3.setClickable(false);
                String demoData;
                demoData = "demoMe," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + "demo3" + "\n";
                byte[] bytes = new byte[0];
                try {
                    bytes = demoData.getBytes("UTF-8");
                    mBActivity.mBluetoothConnection.write(bytes);
                    Log.d(mBActivity.getTAG(), "Message written to Pi: " + demoData);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                while(true){
                    String pimsg = mBActivity.mBluetoothConnection.getMessage();
                    if(pimsg.equals("done3")){
                        demo3.setAlpha(1f);
                        demo3.setClickable(true);
                        break;
                    }

                }




            }
        });





        return view;
    }

    //This fragment should display the maps and should send raspberry ti to the destination


}