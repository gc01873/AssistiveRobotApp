package com.example.bluetooth2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ControlMeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlMeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    private BluetoothConnectActivity mBActivity;

    //Buttons being added to followMe fragment
    //Button send;
    Button followMe;
    Button ControlMe;
    Button MapMe;
    Button stop;
    //EditText write;
    TextView MessageLog;
    Button up;
    Button down;
    Button left;
    Button right;
    private Boolean upPressed=false;
    private Boolean downPressed= false;
    private Boolean leftPressed = false;
    private Boolean rightPressed = false;




    Thread GPS_t;

    FragmentManager fm;

    public ControlMeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ControlMeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ControlMeFragment newInstance(String param1, String param2) {
        ControlMeFragment fragment = new ControlMeFragment();
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
        View view = inflater.inflate(R.layout.fragment_control_me, container, false);
        mBActivity = (BluetoothConnectActivity) getActivity();
        //send = (Button) view.findViewById(R.id.send);
        followMe = (Button) view.findViewById(R.id.FollowMe);
        ControlMe = (Button) view.findViewById(R.id.ControlMe);
        MapMe = (Button) view.findViewById(R.id.Map) ;
        //write = (EditText) view.findViewById(R.id.write);
        MessageLog = (TextView)view.findViewById(R.id.MessageLog);
        stop = (Button) view.findViewById(R.id.stop);
        up  = (Button)view.findViewById(R.id.up_arrow);
        down = (Button)view.findViewById(R.id.down_arrow);
        left = (Button)view.findViewById(R.id.left_arrow);
        right = (Button)view.findViewById(R.id.right_arrow);
        GPS_t = new Thread(new BluetoothConnectActivity.GPSUpdateRunnable(mBActivity.mBluetoothConnection, mBActivity.getGPS()));
        GPS_t.start();


//This will manage the various fragments
        fm = mBActivity.getSupportFragmentManager();

        ControlMe.setAlpha(.5f);
        ControlMe.setClickable(false);

        writeControl("controlMe", "none");
        //writeControl("controlMe", "stop");


       /* //Send button will send data to the pi
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBActivity.mBluetoothConnection!=null) {
                    if (write.getText().toString() != null) {
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
                    }
                }
            }
        });*/

        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS_t.interrupt();
                if (mBActivity.getFollowMeFragment() == null) {
                    mBActivity.setFollowMeFragment(new FollowMeFragment());
                    Log.d(mBActivity.getTAG(),"A New FollowME Fragment has been created");
                    fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getFollowMeFragment())
                            .addToBackStack(null)
                            .commit();
                }
                else {
                    GPS_t.stop();
                    Log.d(mBActivity.getTAG()," FollowMe Fragment of the Bluetooth Activity has been created");
                    fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getFollowMeFragment())
                            .addToBackStack(null)
                            .commit();


                }
            }

        });
        //Map me will call t
        MapMe.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         GPS_t.stop();
                                         if (mBActivity.getMapFragment() == null) {
                                             mBActivity.setMapFragment(new MapFragment());
                                             Log.d(mBActivity.getTAG(),"A New Map Fragment has been created");
                                             fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getMapFragment())
                                                     .addToBackStack(null)
                                                     .commit();
                                         }
                                         else{
                                             GPS_t.stop();
                                             Log.d(mBActivity.getTAG()," Map Fragment of the Bluetooth Activity has been created");
                                             fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getMapFragment())
                                                     .addToBackStack(null)
                                                     .commit();
                                         }
                                     }
                                 }


        );

        stop.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         writeControl("controlMe", "stop");

                                     }
                                 }


        );





        up.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;


            @Override
            public boolean onTouch(View v, MotionEvent event) {


                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 500);
                        up.setAlpha(.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        upPressed =false;
                        writeControl("controlMe", "stop1");
                        mHandler = null;
                        up.setAlpha(1f);
                        Log.d(mBActivity.getTAG(), "up Button lifted");
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    if(upPressed==false) {
                        upPressed=true;
                        writeControl("controlMe", "up");
                    }
                    mHandler.postDelayed(this, 500);
                }

            };

        });
        down.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 500);
                        down.setAlpha(.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        downPressed=false;
                        writeControl("controlMe", "stop");
                        mHandler = null;
                        Log.d(mBActivity.getTAG(), "down Button lifted");
                        down.setAlpha(1f);
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    if (downPressed == false) {
                        downPressed = true;

                        writeControl("controlMe", "down");
                    }
                    mHandler.postDelayed(this, 500);
                }
            };
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 500);
                        left.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        //mHandler.postDelayed(mActionStop,500);
                        //mHandler.removeCallbacks(mActionStop);
                        leftPressed=false;

                        writeControl("controlMe","stop");
                        mHandler = null;
                        left.setAlpha(1f);

                        Log.d(mBActivity.getTAG(), "left Button lifted");
                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    if (leftPressed == false) {
                        leftPressed = true;

                        writeControl("controlMe","left");
                    }

                    mHandler.postDelayed(this, 500);
                }


            };

        });



        right.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 500);
                        right.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        //mHandler.postDelayed(mActionStop,500);
                        //mHandler.removeCallbacks(mActionStop);
                        mHandler = null;
                        rightPressed=false;
                        String GPSData = null;
                        writeControl("controlMe","stop");
                        right.setAlpha(1f);

                        Log.d(mBActivity.getTAG(), "right Button lifted");


                        break;
                }
                return false;
            }
            Runnable mAction = new Runnable() {
                @Override public void run() {
                    if(rightPressed==false) {
                        rightPressed = true;
                    writeControl("controlMe","right");
                        }
                    mHandler.postDelayed(this, 500);

                }
            };
        });
        return view;


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


    public Boolean getUpPressed() {
        return upPressed;
    }

    public void setUpPressed(Boolean upPressed) {
        this.upPressed = upPressed;
    }

    public Boolean getDownPressed() {
        return downPressed;
    }

    public void setDownPressed(Boolean downPressed) {
        downPressed = downPressed;
    }

    public Boolean getLeftPressed() {
        return leftPressed;
    }

    public void setLeftPressed(Boolean leftPressed) {
        this.leftPressed = leftPressed;
    }

    public Boolean getRightPressed() {
        return rightPressed;
    }

    public void setRightPressed(Boolean rightPressed) {
        this.rightPressed = rightPressed;
    }
}