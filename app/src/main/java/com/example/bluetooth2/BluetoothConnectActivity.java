package com.example.bluetooth2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.gms.location.LocationRequest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothConnectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    //Declare the bluetooth adapter for the device
BluetoothAdapter mBluetoothAdapter;
//Widgets for device
Button scan;
Button send;
Button followMe;
TextView showDevices;
ListView lv;
EditText write;
Switch onOff;
TextView MsgTerminal;
public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
public ArrayList<String> mBTDevicesNames = new ArrayList<>();
private GPSService GPS;
public Boolean followMode = false;

//this device will become the raspberry pi I believe
BluetoothDevice mBTDevice;

//ID is the same as the PI
private static final UUID MY_UUID_INSECURE = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
//For logging errors
private static final String TAG = "BluetoothActivity";
public BluetoothConnectionService mBluetoothConnection;


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
                lv.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mBTDevicesNames));
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


        //Widgets declarations
        scan = (Button) findViewById(R.id.scan);
        send  = (Button) findViewById(R.id.send);
        showDevices = (EditText) findViewById(R.id.showDevices);

        write = (EditText) findViewById(R.id.write);

        onOff = (Switch) findViewById(R.id.ONOFFbtn);

        MsgTerminal = (TextView) findViewById(R.id.MsgTerminal);

        lv = (ListView) findViewById(R.id.lv);
        followMe = (Button)findViewById((R.id.followMe));

        MsgTerminal.setVisibility(View.GONE);
        write.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        followMe.setVisibility(View.GONE);


        //Hmm on this page this creates an on click listener
        lv.setOnItemClickListener(BluetoothConnectActivity.this);
        //Create the gps object
        GPS = new GPSService(this);







        //Make the Widgets do something

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Now Attempting to scan for Bluetooth devices (Raspberry Pi)");
                if(mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }

                checkBTPermissions();

                Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(getVisible, 0);


                //May be redundant, We will see
                mBluetoothAdapter.startDiscovery();


//Broadcast receiver initiated
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);

                //This can be another button but it will be in scan for now.

                //Add another broadcastreceiver to show state changes
                int requestCode = 1;
                Intent discoverableIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                //discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(discoverableIntent, requestCode);




            }
        });
send.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if(mBluetoothConnection!=null) {
            if (write.getText().toString() != null) {
               // String SendData = w;
                try {
                    byte[] bytes = write.getText().toString().getBytes("UTF-8");
                    mBluetoothConnection.write(bytes);
                    MsgTerminal.append("Android: "+write.getText().toString() +"\n");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG,"Write method error: "+e.getMessage() );
                    e.printStackTrace();
                }
                //convert editText to byte code
                //use Bluetoothconnection to write i.e send to raspberry pi
                //print text to msg terminal
            }
        }
    }

    });

        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
    if(mBluetoothAdapter==null){
    Log.d(TAG,"No Bluetooth Device detected on this system");
    Toast.makeText(getApplicationContext(), "Turned on",Toast.LENGTH_LONG).show();
} else if(!isChecked){
           // if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Turning on Bluetooth Service");
//May need to move this outside
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);

            // IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            // registerReceiver(mBroadcastReceiver1, BTIntent);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();

      //  }


                }


                else
                {
                    Log.d(TAG, "Turning off Bluetooth Service");
                    mBluetoothAdapter.disable();

                    Toast.makeText(getApplicationContext(), "Bluetooth Turned off",Toast.LENGTH_LONG).show();

                   // IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    //registerReceiver(mBroadcastReceiver1, BTIntent);
                }
            }
        });

//



        followMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followMode =true;
Thread GPS_t = new Thread(new GPSRunnable(mBluetoothConnection,followMode,GPS));
GPS_t.start();
            }

        });

        //May need to change this but we will see
        // Register for broadcasts when a device is discovered.
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(receiver, filter);




    }

      /* NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
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
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    //OKAYY METHOD WHEN WE CLICK ON A DEVICE
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
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
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
                Log.e(TAG,"Attemping to sleep so that the raspbPi accept thread is started first");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Write for if the device is the raspberry pi
            mBluetoothConnection = new BluetoothConnectionService(BluetoothConnectActivity.this,mBluetoothAdapter,mBTDevice,lv,write,MsgTerminal,send);
            mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);

            lv.setVisibility(View.GONE);
            MsgTerminal.setVisibility(View.VISIBLE);
            write.setVisibility(View.VISIBLE);
            send.setVisibility(View.VISIBLE);
            followMe.setVisibility(View.VISIBLE);
            scan.setVisibility(View.GONE);
            onOff.setVisibility(View.GONE);

///In this we have passed in also the bluetooth adapter and the Bluetooth Device

            }

        }




    @Override
    protected void onDestroy() {
        super.onDestroy();
// Don't forget to unregister the ACTION_FOUND receiver.
if(receiver1!=null){
    unregisterReceiver(receiver1);
}
        if(receiver!=null){
            unregisterReceiver(receiver);
        }

    }


public class GPSRunnable implements Runnable{

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
                    String GPSData = Double.toString(l.getLatitude())+","+ Double.toString(l.getLongitude())+","+Double.toString(l.getAccuracy())+"\n";
                    byte[] bytes = new byte[0];
                    try {
                        bytes = GPSData.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mBluetoothConnection.write(bytes);
                    MsgTerminal.append("Android Sent: "+write.getText().toString() +"\n");
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



}
