package com.example.bluetooth2;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.service.controls.Control;
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
public class MapFragment extends Fragment implements OnMapReadyCallback {

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
    Button MapMe;
    EditText write;
    TextView MessageLog;

    Thread GPS_t;

    FragmentManager fm;
    MapView mMapView;
    private GoogleMap googleMap;

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
        send = (Button) view.findViewById(R.id.send);
        followMe = (Button) view.findViewById(R.id.FollowMe);
        ControlMe = (Button) view.findViewById(R.id.ControlMe);
        MapMe = (Button) view.findViewById(R.id.Map);
        write = (EditText) view.findViewById(R.id.write);
        // MessageLog = (TextView)view.findViewById(R.id.MessageLog);
//This will manage the various fragments
        fm = mBActivity.getSupportFragmentManager();

//This will make the map button unclickable
        MapMe.setAlpha(.5f);
        MapMe.setClickable(false);
        //This should give us the gps from the activity
        if(mBActivity.getGPS()!=null){
            mapGps = mBActivity.getGPS();
            Log.d(mBActivity.getTAG(),"GPS Service is not null!");
        }
        else{
            Log.e(mBActivity.getTAG(),"GPS Service seems to be null!");
        }
        Location currentL = mapGps.getLastLocation();


        //Send button will send data to the pi
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBActivity.mBluetoothConnection != null) {
                    if (write.getText().toString() != null) {
                        // String SendData = w;
                        try {
                            byte[] bytes = write.getText().toString().getBytes("UTF-8");
                            mBActivity.mBluetoothConnection.write(bytes);
                            MessageLog.append("Android: " + write.getText().toString() + "\n");
                        } catch (UnsupportedEncodingException e) {
                            Log.e(mBActivity.getTAG(), "Write method error: " + e.getMessage());
                            e.printStackTrace();
                        }
                        //convert editText to byte code
                        //use Bluetoothconnection to write i.e send to raspberry pi
                        //print text to msg terminal
                    }
                }
            }
        });

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
        MapMe.setOnClickListener(new View.OnClickListener() {
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


        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(mBActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mBActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng myLocation = new LatLng(currentL.getLatitude(), currentL.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(myLocation).title("My location").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        return view;
    }

    //This fragment should display the maps and should send raspberry ti to the destination

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}