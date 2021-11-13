package com.example.bluetooth2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GnssAntennaInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BluetoothScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothScanFragment extends Fragment implements AdapterView.OnItemClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BluetoothConnectActivity mBActivity;


    //Declare buttons

    Button scan;
    TextView showDevices;
    Switch onOff;
    ListView lv;
    String TAG;

//This should send the list view info to the main bluetooth class
    private ItemViewModel viewModel;

    public BluetoothScanFragment() {
        super(R.layout.fragment_bluetooth_scan);
        Log.d(TAG+"Bluetooth Fragment ","Fragment layout should be applid." );
        // Required empty public constructor
    }


    // Create a BroadcastReceiver for ACTION_FOUND.
  /*  private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //mBTDevices.clear();
            // mBTDevicesNames.clear();


            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //This should add any devices near to the BTDevices list
                mBActivity.mBTDevices.add(device);


                mBActivity.mBTDevicesNames.add(device.getName() + "\n" + device.getAddress());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                //Change to list view?
                lv.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mBActivity.mBTDevicesNames));
                //lv.notifyDataSetChanged();

            }

            //THIS SHOULD HANDLE DUPLICATES AFTER PRESSING DISCOVERY TWICE or MORE
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Log.d(TAG,"device search finished");
                Log.d(TAG,"end list size: "+mBActivity.mBTDevices.size());
                mBActivity.unregisterReceiver(this); //add this line
            }
        }
    };
//mBroadcastReceiver4

    private final BroadcastReceiver receiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 Cases
                //case 1
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {


                    Log.d(TAG, "BroadcastReceiver:BOND_BONDED with "+ mDevice.getName());
                }

                //case 2
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {

                    Log.d(TAG, "BroadcastReceiver:BOND_BONDING with "+ mDevice.getName());
                }

                //case 3
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {

                    Log.d(TAG, "BroadcastReceiver:BOND_NONE");
                }


            }
        }
    };*/


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BluetoothScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BluetoothScanFragment newInstance(String param1, String param2) {
        BluetoothScanFragment fragment = new BluetoothScanFragment();
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

        mBActivity = (BluetoothConnectActivity) getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth_scan, container, false);
        scan = (Button) view.findViewById(R.id.scan);
        onOff = (Switch) view.findViewById(R.id.ONOFFbtn);
        showDevices = (EditText) view.findViewById(R.id.showDevices);
        lv = (ListView) view.findViewById(R.id.lv);
        lv.setOnItemClickListener(mBActivity);
        TAG = mBActivity.getTAG();





        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(mBActivity.getTAG(), "Now Attempting to scan for Bluetooth devices (Raspberry Pi) from Fragment!");
                if(mBActivity.mBluetoothAdapter.isDiscovering()){
                    mBActivity.mBluetoothAdapter.cancelDiscovery();
                }

                mBActivity.checkBTPermissions();

                Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(getVisible, 0);


                //May be redundant, We will see
                mBActivity.mBluetoothAdapter.startDiscovery();


//Broadcast receiver initiated
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                mBActivity.registerReceiver(mBActivity.getReceiver(), filter);

                //This can be another button but it will be in scan for now.

                //Add another broadcastreceiver to show state changes
                int requestCode = 1;
                Intent discoverableIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(discoverableIntent, requestCode);




            }
        });



        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(mBActivity.mBluetoothAdapter==null){
                    Log.d(TAG,"No Bluetooth Device detected on this system");
                    Toast.makeText(mBActivity.getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
                } else if(!isChecked){
                    // if (!mBluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Turning on Bluetooth Service");
//May need to move this outside
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);

                    // IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    // registerReceiver(mBroadcastReceiver1, BTIntent);
                    Toast.makeText(mBActivity.getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();

                    //  }


                }


                else
                {
                    Log.d(TAG, "Turning off Bluetooth Service");
                    mBActivity.mBluetoothAdapter.disable();

                    Toast.makeText(mBActivity.getApplicationContext(), "Bluetooth Turned off",Toast.LENGTH_LONG).show();

                     IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    mBActivity.registerReceiver(mBActivity.getReceiver1(), BTIntent);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        //This should share the listview data of the Fragment 1 with the main activity
        viewModel.selectItem(lv);
    }


    //OKAYY METHOD WHEN WE CLICK ON A DEVICE
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBActivity.mBluetoothAdapter.cancelDiscovery();


        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBActivity.mBTDevices.get(i).getName();
        String deviceAddress = mBActivity.mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);


        //WE MAY NEED AN IF STATEMENT FOR IF THE DEVICE IS ALREADY PAIRED

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBActivity.mBTDevices.get(i).createBond();

            mBActivity.mBTDevice = mBActivity.mBTDevices.get(i);
            //We'll get there






//This should activate the intent filter
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

            mBActivity.registerReceiver(mBActivity.getReceiver1(), filter);

            //SO WE MUST FIRST PAIR THE RASPBERRY PI WITH THE PI
//This should also change the list view to the edit text window which shows the texts from the raspberry pi and the other
            try {
                Log.e(TAG,"Attemping to sleep so that the raspbPi accept thread is started first");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBActivity.Connected=mBActivity.startBluetooth();
            if(mBActivity.Connected){
                //code to change the activity fragment.
            }

        }

    }







    }


