package com.example.bluetooth2;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class BluetoothConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    //Declare the bluetooth adapter for the device
BluetoothAdapter mBluetoothAdapter;
//Widgets for device
//Button scan;
Button send;
Button followMe;
ListView lv;
EditText write;
TextView MsgTerminal;
public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
public ArrayList<String> mBTDevicesNames = new ArrayList<>();
private ArrayAdapter<String> listAdapter;
Boolean Connected=false;
private GPSService GPS;
public Boolean followMode = false;
//this device will become the raspberry pi
BluetoothDevice mBTDevice;
//ID is the same as the PI
private static final UUID MY_UUID_INSECURE = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
ProgressDialog dialog;

//For logging errors
private static final String TAG = "BluetoothActivity";
public BluetoothConnectionService mBluetoothConnection;

/*THESE FRAGMENTS MAY BECOME NULL SO THEIR STATES MUST BE SAVED!


Things to be saved:
1.The listview
2.The bluetooth connection made
3.The states of the fragments



 */


    //Fragments that will be used by program
    private FollowMeFragment followMeFragment;


    private MapFragment mapFragment;
    private BluetoothScanFragment bluetoothScanFragment;

    private ControlMeFragment controlMeFragment;
    //Needs to make one more fragment the controlMe Fragment


FragmentManager  fragmentManager = getSupportFragmentManager();

    //This will be used to communicate with the bluetooth scan fragment
    private ItemViewModel viewModel;

public BluetoothConnectActivity(){
    super(R.layout.activity_connectblu);
}



//Declare Broadcast receiver 1 used for discovering new devices




    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
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
                mBTDevices.add(device);


                mBTDevicesNames.add(device.getName() + "\n" + device.getAddress());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                //Change to list view?
                listAdapter=new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mBTDevicesNames);
                lv.setAdapter(listAdapter);
                //lv.notifyDataSetChanged();

            }

            //THIS SHOULD HANDLE DUPLICATES AFTER PRESSING DISCOVERY TWICE or MORE
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
               Log.d(TAG,"device search finished");
                Log.d(TAG,"end list size: "+mBTDevices.size());
                unregisterReceiver(this); //add this line
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
    };


//Declare the buttons for the device
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectblu);

//Bluetooth adapter for the device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        GPS = new GPSService(this);
        dialog = new ProgressDialog(this);
//

        try {
            if(savedInstanceState==null) {
                 bluetoothScanFragment = new BluetoothScanFragment();
                fragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        //.add(R.id.fragment_container_view,BluetoothScanFragment.class,null)
                        .add(R.id.fragment_container_view,bluetoothScanFragment,null)
                        .addToBackStack(null)// name can be null
                        .commit();
                Log.d(TAG, "Fragment first display");
            }
            else if(savedInstanceState!=null){
              //  onRestoreInstanceState(savedInstanceState);

            }
        }
        catch(Exception e){
            Log.e(TAG,"Error initializing Fragment!");
        }

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        viewModel.getSelectedItem().observe(this, item -> {
            // Perform an action with the latest item data
            lv = (ListView) item;


          //  Parcelable state = lv.onSaveInstanceState();
           // lv.onRestoreInstanceState(state);

        });





/*
        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followMode =true;
Thread GPS_t = new Thread(new GPSRunnable(mBluetoothConnection,followMode,GPS));
GPS_t.start();
            }

        });
*/
        //May need to change this but we will see
        // Register for broadcasts when a device is discovered.
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(receiver, filter);




    }

      /* NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
     void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            }
            if (permissionCheck != 0) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.BLUETOOTH_CONNECT}, 1001); //Any number
                }
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();


        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);


        //WE MAY NEED AN IF STATEMENT FOR IF THE DEVICE IS ALREADY PAIRED

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            //We'll get there


//This should activate the intent filter
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

            registerReceiver(receiver1, filter);

            //SO WE MUST FIRST PAIR THE RASPBERRY PI WITH THE PI
//This should also change the list view to the edit text window which shows the texts from the raspberry pi and the other
            try {
                Log.e(TAG, "Attemping to sleep so that the raspbPi accept thread is started first");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Connected = startBluetooth();
            Log.d(TAG, "possible error in onclick method");
            //and name of device is rasp
            if (Connected) {
                //code to change the activity fragment.

                fragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container_view, FollowMeFragment.class, null)
                        .addToBackStack(null)// name can be null
                        .commit();
            }
            else
            {
                Log.d(TAG,"Unsupported Bluetooth Device!");
                dialog.setMessage("Try selecting another supoorted device.");
                dialog.show();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }

        }
    }



        public Boolean startBluetooth(){
         try {
             Log.d(TAG,"initialiting start Bluetooth");
             mBluetoothConnection = new BluetoothConnectionService(BluetoothConnectActivity.this, mBluetoothAdapter, mBTDevice, write);
             mBluetoothConnection.startClient(mBTDevice, MY_UUID_INSECURE);
             byte[] buffer = new byte[1024];  // buffer store for the stream

             int bytes;
             String uuidtest = "94f39d29-7d6d-437d-973b-fba39e49d4ee";
             Thread.sleep(2000);
             String pimsg = mBluetoothConnection.getMessage();
             Log.d(TAG,"Received uuid string from Pi");
             Log.d(TAG,pimsg);


             while(true) {
                 if (uuidtest.equals(pimsg)) {
                     mBluetoothConnection.write("True".getBytes("UTF-8"));
                     Log.d(TAG,"Correct Device!");

                     return true;
                 } else {
                     mBluetoothConnection.write("False".getBytes("UTF-8"));
                     Log.d(TAG,"Incorrect Uuid!");
                     return false;
                 }
             }

         }catch(Exception e){
             Log.e(TAG,"Error trying to connect with Bluetooth");
             return false;
         }

        }
        //Verify the pi is the device
       /* public Boolean verifyPi() throws UnsupportedEncodingException, InterruptedException {
         String uuidtest = "94f39d29-7d6d-437d-973b-fba39e49d4ee";

            while(true) {
                if (mBluetoothConnection != null){
                    if (mBluetoothConnection.read() != null) {
                        mBluetoothConnection.write(uuidtest.getBytes("UTF-8"));
                        if (mBluetoothConnection.read().equals("True")) {
                            return true;
                        } else if (mBluetoothConnection.read().equals("False")) {
                            return false;
                        } else {
                            if (progressDialog == null) {
                                progressDialog.setMessage("Verifying connection with Pi");
                                progressDialog.show();
                            }
                        }
                    }
            }
            }

        }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
// Don't forget to unregister the ACTION_FOUND receiver.
        try {
            if (receiver1 != null) {
                unregisterReceiver(receiver1);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
        }catch(Exception e){
            Log.e(TAG,"Receivers are already destroyed or do not exist in the first place");
        }

    }




public static class GPSRunnable implements Runnable{
//This thread is to be used specifically for follow Me method. Control me and map me threads can be added later
        BluetoothConnectionService mBluetoothConnection;
        Boolean followMode;
        GPSService GPS;
        public GPSRunnable(BluetoothConnectionService mBluetoothConnection,Boolean followMode,GPSService GPS){
            this.followMode = followMode;
            this.mBluetoothConnection = mBluetoothConnection;
            this.GPS = GPS;
        }

    @Override
    public void run() {
Log.d(TAG,"Runnable implemented");
        if(mBluetoothConnection!=null) {
            //LocationRequest.PRIORITY_HIGH_ACCURACY
            GPS.setLocationRequests(2000,1000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            GPS.startLocationUpdates();
            while(followMode){

                Location l = GPS.getLastLocation();
                if(l!=null) {
                    Log.d(TAG, "Current Location");
                    Log.d(TAG, "Latitude: " + Double.toString(l.getLatitude()));
                    Log.d(TAG, "Longitude:  " + Double.toString(l.getLongitude()));
                    Log.d(TAG, "Accuracy: " + Double.toString(l.getAccuracy()));
                    String GPSData = "followMe,"+ Double.toString(l.getLatitude())+","+ Double.toString(l.getLongitude())+","+Double.toString(l.getAccuracy())+"\n";
                    byte[] bytes = new byte[0];
                    try {
                        bytes = GPSData.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mBluetoothConnection.write(bytes);
                    //This will be referenced with the next fragment class
                    //MsgTerminal.append("Android Sent: "+write.getText().toString() +"\n");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                //}
            }

        }

    }
}
//getter and setter methods to return the private variables used in the fragments else it will not work
    //returns the private TAG variable
    public static String getTAG() {
        return TAG;
    }
//Returns the 1st private receiver
    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    public BroadcastReceiver getReceiver1() {
        return receiver1;
    }

    public GPSService getGPS() {
        return GPS;
    }


    public FollowMeFragment getFollowMeFragment() {
        return followMeFragment;
    }

    public MapFragment getMapFragment() {
        return mapFragment;
    }

    public BluetoothScanFragment getBluetoothScanFragment() {
        return bluetoothScanFragment;
    }


    public void setFollowMeFragment(FollowMeFragment followMeFragment) {
        this.followMeFragment = followMeFragment;
    }

    public void setMapFragment(MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    public void setBluetoothScanFragment(BluetoothScanFragment bluetoothScanFragment) {
        this.bluetoothScanFragment = bluetoothScanFragment;
    }

    public ControlMeFragment getControlMeFragment() {
        return controlMeFragment;
    }

    public void setControlMeFragment(ControlMeFragment controlMeFragment) {
        this.controlMeFragment = controlMeFragment;
    }

    }


//}
