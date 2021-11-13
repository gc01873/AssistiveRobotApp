package com.example.bluetooth2;

        import android.bluetooth.BluetoothAdapter;
        import android.app.ProgressDialog;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothServerSocket;
        import android.bluetooth.BluetoothSocket;
        import android.content.Context;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.nio.charset.Charset;
        import java.util.UUID;

public class BluetoothConnectionService {
    //The same adapter from thhe BluetoothConnectActivity
    BluetoothAdapter mBluetoothAdapter;
    private static final String TAG = "BluetoothConnectionServ";
    private static final UUID MY_INSECURE_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    private static final String uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee";

    private static final String appName = "AssistedBot app";
//thread used when in server mode
    private AcceptThread  mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    Context mContext;
    ListView lv;
    EditText write;



    //BluetoothSocket mSocket;

    private ConnectedThread mConnectedThread;
    public BluetoothConnectionService(Context context, BluetoothAdapter mBluetoothAdapter, BluetoothDevice mmDevice, EditText write) {
        this.mContext = context;

        //These lines should do the same thing because they are getting the same device
        // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mmDevice = mmDevice;

        this.write = write;




        //Should work
        //startClient(mmDevice,MY_INSECURE_UUID);
    }

    private class AcceptThread extends Thread{
        //The local server socket -this is when we are in server mode
        private final BluetoothServerSocket mmServerSocket;


        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            try {
                //Create a new listening server
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_INSECURE_UUID);
                Log.d(TAG, "AcceptThread: Accept thread started");
                Log.d(TAG, "AcceptThread: Setting Up Server using " + MY_INSECURE_UUID);
            }catch(IOException e){
                Log.e(TAG,"Error in Accept Thread: " + e.getMessage());

            }

            mmServerSocket =tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running");

            BluetoothSocket socket = null;

            Log.d(TAG, "run: RFComm Server start....");
try {


    socket = mmServerSocket.accept();
}catch(IOException e){
    Log.e(TAG,"run: Accept thread ran into an error "+ e.getMessage());
}
if(socket!=null){
    //
    connected(socket,mmDevice);
}
Log.i(TAG,"END mAcceptThhread");
        }

        public void cancel(){
            Log.d(TAG,"Canel: Cancelling AcceptThread");
            try{
                mmServerSocket.close();
            }
            catch(IOException e){
                Log.e(TAG,"cancel: Close of Acceptthread ServerSocket failed. " +e.getMessage());

            }
        }



    }


    private class ConnectThread extends Thread{

        private BluetoothSocket mmSocket;


        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG,"ConnectThread: started");
            mmDevice = device;
            deviceUUID = uuid;

        }

        public void run(){
            BluetoothSocket tmp =null;
            Log.i(TAG,"RUN mConnectThread");


            //Here we try to connect with the device via a Bluetooth Socket

            try{
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_INSECURE_UUID);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }
            catch(IOException e){
                Log.e(TAG, "ConnectThread: Could not create an InsecureRFCommSocket "+ e.getMessage());
            }

            mmSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            try{
                Log.d(TAG,"Attempting to connect");
                mmSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            }catch(IOException e){
                try{
                    mmSocket.close();
                    Log.d(TAG,"run:Closed Socket");
                }
                catch(IOException e1){

                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_INSECURE_UUID );
            }

            connected(mmSocket,mmDevice);

        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }



    }

    public synchronized void start(){
        Log.d(TAG,"starting threading");

        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }


    }


    /**
     AcceptThread starts and sits waiting for a connection.
     Then ConnectThread starts and attempts to make a connection with the other devices AcceptThread.
     **/

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
        //this.mSocket = mConnectedThread.mmSocket;

        Log.d(TAG, "startClient: Client Start method complete.");

    }

    /**
     Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     receiving incoming data through input/output streams respectively.
     **/

    //May need to make write and read in seperate threads
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public String message;



        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    message = incomingMessage;

                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }


    }
    public InputStream getMmInStream() {
        return mConnectedThread.mmInStream;
    }
    public String getMessage(){
        return mConnectedThread.message;
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.");
        //perform the write
        mConnectedThread.write(out);
    }









}