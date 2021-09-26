package com.example.bluetooth2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button Bluetooth;
    private Button GPSView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Bluetooth = (Button)findViewById(R.id.Bluetooth);
        Bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBluetoothConnectActivity();
                finish();
            }


        });

        GPSView = (Button)findViewById(R.id.GPSView);
        GPSView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGPSActivity();
                finish();
            }


        });
    }





    public void openBluetoothConnectActivity(){
        Intent intent = new Intent(this,BluetoothConnectActivity.class);
        startActivity(intent);

    }

    public void openGPSActivity(){
        Intent intent = new Intent(this,GPSActivity.class);
        startActivity(intent);

    }
}