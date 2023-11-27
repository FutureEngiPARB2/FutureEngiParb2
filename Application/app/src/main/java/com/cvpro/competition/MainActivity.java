package com.cvpro.competition;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.cvpro.competition.ImageClassification.Image_Classification;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    ImageButton Setting;
    Button Open_challenge,ImageClass,DataCollection;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION IS GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    Log.d("USB","USB Ready");
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION IS NOT GRANTED
                    Toast.makeText(context, "USB Permission is not Granted", Toast.LENGTH_SHORT).show();
                    Log.d("USB","USB Permission is not Granted");
                    break;
                case UsbService.ACTION_NO_USB: // NO - USB CONNECTION
                    Toast.makeText(context, "No USB Connection", Toast.LENGTH_SHORT).show();
                    Log.d("USB","No USB Connection");
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB Disconnected", Toast.LENGTH_SHORT).show();
                    Log.d("USB","USB Disconnected");
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB is not supported to device", Toast.LENGTH_SHORT).show();
                    Log.d("USB","USB is not supported to device");
                    break;
            }
        }
    };
    public static UsbService usbService;
    private MainActivity.MyHandler mnHandler;
    private TextView display;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mnHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dialog dialog = new Dialog(MainActivity.this);
        mnHandler = new MainActivity.MyHandler(this);

        ImageClass= findViewById(R.id.second_challenge);
        ImageClass.setOnClickListener(view  -> openImageClassification());

        DataCollection = findViewById(R.id.datacollection);
        DataCollection.setOnClickListener(view ->{
            Intent intent = new Intent(this, com.cvpro.competition.DataCollection.DataCollection.class);
            startActivity(intent);
        });

        Setting=findViewById(R.id.setting_button);
        Setting.setOnClickListener(view  -> openSettings());

        Open_challenge = findViewById(R.id.openchallenge);

        Open_challenge.setOnClickListener(v -> {
            Intent intent = new Intent(this,Sample_Challenge.class);
            startActivity(intent);
        });
    }

    private void openSettings() {
        Intent intent = new Intent(this, Selecting_Model.class);
        startActivity(intent);
    }

    private void openImageClassification() {
        Intent intent = new Intent(this, Image_Classification.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(usbConnection); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(ServiceConnection serviceConnection) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, UsbService.class);
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, UsbService.class);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler
    {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}