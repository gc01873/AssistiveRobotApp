package com.example.bluetooth2;

import android.content.pm.PackageManager;
import android.os.Bundle;

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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.UnsupportedEncodingException;

import android.Manifest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FollowMeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FollowMeFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BluetoothConnectActivity mBActivity;

    //Buttons being added to followMe fragment
    Button Start;
    Button followMe;
    Button ControlMe;
    Button MapMe;
    EditText write;
    TextView MessageLog;
    MapView mMapView;
    private GoogleMap googleMap;

//This thread will run the code.
    Thread GPS_t;

FragmentManager fm;
//

    public FollowMeFragment() {
        // Required empty public constructor
        super(R.layout.fragment_follow_me);
    }


    public static FollowMeFragment newInstance(String param1, String param2) {
        FollowMeFragment fragment = new FollowMeFragment();
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
        View view = inflater.inflate(R.layout.fragment_follow_me, container, false);
        mBActivity = (BluetoothConnectActivity) getActivity();
         Start = (Button) view.findViewById(R.id.send);
         followMe = (Button) view.findViewById(R.id.FollowMe);
         ControlMe = (Button) view.findViewById(R.id.ControlMe);
         MapMe = (Button) view.findViewById(R.id.Map) ;
         write = (EditText) view.findViewById(R.id.write);
         MessageLog = (TextView)view.findViewById(R.id.MessageLog);
         //This will manage the various fragments
         fm = mBActivity.getSupportFragmentManager();
        Thread GPS_t = new Thread(new BluetoothConnectActivity.GPSRunnable(mBActivity.mBluetoothConnection, mBActivity.followMode, mBActivity.getGPS()));
        //writeControl("followMe", "none");

//Send button will send data to the pi
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBActivity.mBluetoothConnection != null) {
                   /* if (write.getText().toString() != null) {
                        // String SendData = w;
                        try {
                            byte[] bytes = write.getText().toString().getBytes("UTF-8");
                            mBActivity.mBluetoothConnection.write(bytes);
                            MessageLog.append("Android: "+write.getText().toString() +"\n");
                        } catch (UnsupportedEncodingException e) {
                            Log.e(mBActivity.getTAG(),"Write method error: "+e.getMessage() );
                            e.printStackTrace();
                        }
                        //convert editText to byte code
                        //use Bluetoothconnection to write i.e send to raspberry pi
                        //print text to msg terminal
                    }*/
                    try {
                        if (Start.getText().equals("Start")) {
                            Thread GPS_t = new Thread(new BluetoothConnectActivity.GPSRunnable(mBActivity.mBluetoothConnection, mBActivity.followMode, mBActivity.getGPS()));
                            GPS_t.start();
                            Start.setText("Stop");
                        } else if(Start.getText().equals("Stop")) {

                            GPS_t.interrupt();
                            String GPSData = "inactive,"+ Double.toString(mBActivity.getGPS().getLastLocation().getLatitude())+","+ Double.toString(mBActivity.getGPS().getLastLocation().getLongitude())+","+Double.toString(mBActivity.getGPS().getLastLocation().getAccuracy())+"\n";
                            byte[] bytes = GPSData.getBytes("UTF-8");
                            mBActivity.mBluetoothConnection.write(bytes);
                            Start.setText("Start");
                        }
                    } catch (Exception e) {
                        Log.e(mBActivity.getTAG(), "Write method error: check start method in follow me ");
                        e.printStackTrace();

                    }
                }
            }
        });


        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This will make the followMe button greyed out and it will also indicate that we are in the followMe Fragment
                //followMe.setAlpha(.5f);
                //followMe.setClickable(false);

                if(!mBActivity.followMode) {
                    mBActivity.followMode = true;
                    Log.d(mBActivity.getTAG()," mBActivity.followmode is called" );

                    //GPS Runnable was mad static. This should not be an issue NOW because it is only ran when follow me is pushed!
                    Thread GPS_t = new Thread(new BluetoothConnectActivity.GPSRunnable(mBActivity.mBluetoothConnection, mBActivity.followMode, mBActivity.getGPS()));
                    Log.d(mBActivity.getTAG()," Thread_GPS is instantiated" );

                    GPS_t.start();
                    Log.d(mBActivity.getTAG()," Thread_GPS is started" );
                }
                else {if(mBActivity.followMode){
                    mBActivity.followMode = false;
                    GPS_t.interrupt();

                }
                    mBActivity.followMode = !mBActivity.followMode;
                    //GPS_t.stop();
                    //String GPSData = "inactive,"+ Double.toString(mBActivity.getGPS().getLastLocation().getLatitude())+","+ Double.toString(mBActivity.getGPS().getLastLocation().getLongitude())+","+Double.toString(mBActivity.getGPS().getLastLocation().getAccuracy())+"\n";



                }
            }

        });
        //Map me will call t
        MapMe.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         if (mBActivity.getMapFragment() == null) {
                                             mBActivity.setMapFragment(new MapFragment());
                                             Log.d(mBActivity.getTAG(),"A New Map Fragment has been created");
                                             fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getMapFragment())
                                                     .addToBackStack(null)
                                                     .commit();
                                         }
                                         else{
                                             Log.d(mBActivity.getTAG()," Map Fragment of the Bluetooth Activity has been created");
                                             fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getMapFragment())
                                                     .addToBackStack(null)
                                                     .commit();
                                         }
                                     }
                                 }

        );

        ControlMe.setOnClickListener(new View.OnClickListener() {

                                         @Override
                                         public void onClick(View view) {
                                             if(mBActivity.getFollowMode()==true){
                                                 mBActivity.followMode =false;


                                             }
                                             if (mBActivity.getControlMeFragment() == null) {
                                                 mBActivity.setControlMeFragment(new ControlMeFragment());
                                                 Log.d(mBActivity.getTAG(),"A New ControlMe Fragment has been created");
                                                 fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getControlMeFragment())
                                                         .addToBackStack(null)
                                                         .commit();
                                             }
                                             else{
                                                 Log.d(mBActivity.getTAG()," ControlMe fragment of the Bluetooth Activity has been created");
                                                 fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getControlMeFragment())
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
                //LatLng myLocation = new LatLng(currentL.getLatitude(), currentL.getLongitude());
               // googleMap.addMarker(new MarkerOptions().position(myLocation).title("My location").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(12).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });





        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public void writeControl(String mode,String action){
        String GPSData = null;
        /*if (mBActivity.getGPS().getLastLocation() == null) {
            GPSData = mode+"," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + action + "\n";
        } else {
            GPSData = mode+"," + Double.toString(mBActivity.getGPS().getLastLocation().getLatitude()) + "," + Double.toString(mBActivity.getGPS().getLastLocation().getLongitude()) + "," + Double.toString(mBActivity.getGPS().getLastLocation().getAccuracy()) + "," + action + "\n";
        }

         */
        GPSData = mode+"," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + Double.toString(0.0) + "," + action + "\n";
        byte[] bytes = new byte[0];
        try {
            bytes = GPSData.getBytes("UTF-8");
            mBActivity.mBluetoothConnection.write(bytes);
            Log.d(mBActivity.getTAG(), "Message written to Pi: " + GPSData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}