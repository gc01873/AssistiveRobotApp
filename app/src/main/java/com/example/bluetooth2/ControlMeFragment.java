package com.example.bluetooth2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
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
    Button send;
    Button followMe;
    Button ControlMe;
    Button MapMe;
    EditText write;
    TextView MessageLog;



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
        send = (Button) view.findViewById(R.id.send);
        followMe = (Button) view.findViewById(R.id.FollowMe);
        ControlMe = (Button) view.findViewById(R.id.ControlMe);
        MapMe = (Button) view.findViewById(R.id.Map) ;
        write = (EditText) view.findViewById(R.id.write);
        MessageLog = (TextView)view.findViewById(R.id.MessageLog);
//This will manage the various fragments
        fm = mBActivity.getSupportFragmentManager();

        ControlMe.setAlpha(.5f);
        ControlMe.setClickable(false);



        //Send button will send data to the pi
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
        });

        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBActivity.getFollowMeFragment() == null) {
                    mBActivity.setFollowMeFragment(new FollowMeFragment());
                    Log.d(mBActivity.getTAG(),"A New FollowME Fragment has been created");
                    fm.beginTransaction().replace(R.id.fragment_container_view,mBActivity.getFollowMeFragment())
                            .addToBackStack(null)
                            .commit();
                }
                else {
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
        return view;


    }
}