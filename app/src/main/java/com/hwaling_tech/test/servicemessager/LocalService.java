package com.hwaling_tech.test.servicemessager;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Random;
import me.palazzetti.adktoolkit.AdkManager;
import me.palazzetti.adktoolkit.response.AdkMessage;
import android.os.Handler;


public class LocalService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private AdkManager mAdkManager;
    private AdkMessage response;
     private String readValue = "You are in Service";
    private String writeResult = "Fault";
   // private String ADKReceiveData;
    private int count = 0;
   // private AdkReadTask mAdkReadTask;

//    private static final String ACTION="android.hardware.usb.action.USB_ACCESSORY_ATTACHED";
    private BroadcastReceiver yourReceiver;


    //public Callbacks activity;

    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
           // millis = System.currentTimeMillis() - startTime;
            //activity.updateClient(millis); //Update Activity (client) by the implementd callback
           // activity.receiveFromADK(readValue);
            handler.postDelayed(this, 100);
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocalService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        try{
            mAdkManager.open();
              Toast.makeText(this, "mAdkManager Opened", Toast.LENGTH_SHORT).show();
        }catch (Exception ex){
              Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "InBinding", Toast.LENGTH_SHORT).show();
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mAdkManager.open();
        Toast.makeText(this, "OnStartCommand", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mAdkManager = new AdkManager(this);
           registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());
            Toast.makeText(this, "ADK started", Toast.LENGTH_SHORT).show();
        }catch (Exception ex)
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    public String getADKread(){
        try {
            readValue ="";
            if (mAdkManager.serialAvailable()) {
                response = mAdkManager.read();
                readValue = response.getString();
               // readValue = String.valueOf(count) + " : " + readValue;
               // count++;
            } else {
                readValue = "Disconnect";
            }
        }catch (Exception ex){
            readValue = ex.getMessage(); //  Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
            return  readValue;
        }

    public String getADKsend(String sendtxt){
        /*
        if (mAdkManager.serialAvailable())
        {
          mAdkManager.write(txt);
        }else {
            writeResult = "Disconnect";
        }
        */
        //Remote function call can't use Toast.maketext
        //Toast.makeText(this, "InServer:", Toast.LENGTH_SHORT).show();  error
        writeResult ="";
        writeResult = String.valueOf(count) + " : " + sendtxt;
        mAdkManager.write(sendtxt);
        count++;
        return  writeResult;
    }

    @Override
    public void onDestroy() {
        mAdkManager.close();
        unregisterReceiver(mAdkManager.getUsbReceiver());
        super.onDestroy();
    }

    private void showSuccessfulBroadcast() {
        Toast.makeText(this, "Broadcast Successful!!!", Toast.LENGTH_SHORT).show();
    }
/*
    public interface Callbacks{
        public void updateClient(String readData);
    }
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }
   */

}
