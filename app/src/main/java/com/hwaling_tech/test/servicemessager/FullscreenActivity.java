package com.hwaling_tech.test.servicemessager;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ServiceConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//service test

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * public class FullscreenActivity extends AppCompatActivity implements LocalService.Callbacks {
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private LocalService mService;
    private boolean mBound = false;
    private String readValue ="";
    private String txt = "";
    private String sendtxt ="";
    private AdkReadTask mAdkReadTask;
    private AdkWriteTask mAdkWriteTask;
    public static TextView textRcv, textMessage;
    public static TextView textATemp, textHumdy, textConductivity, textTemperature;

    private String strATemp = "0.00";
    private String strHmdty = "0.00";
    private String strRawCndct = "0.00";
    private String strRawTemp = "0.00";
    private String strCompTemp = "0.00";
    private String strCalCndct = "0.00";
    private String strCalCl = "0.00";
    private String strCompCl = "0.00";

    private double db_ATemp=0.0, db_Hmdty=0.0, db_RawCndct=0.0, db_Conductivity=0.0, db_RawTemp=0.0, db_CompTemp=0.0, db_CalCndct=0.0,db_CalCl = 0.0,  db_CompCl = 0.0;
    private double ParamA[] = { 0.0, 0.0, 0.0 };

    private static final boolean AUTO_HIDE = true;
    private Intent serviceIntent;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
    //             Delayed removal of status and navigation bar
    //             Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

           Toast.makeText(FullscreenActivity.this, "Hello world!", Toast.LENGTH_SHORT).show();
           /*
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            */
           // Intent intent = new Intent(FullscreenActivity.this, LocalService.class);
           // bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            return false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        serviceIntent = new Intent(FullscreenActivity.this, LocalService.class);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        textRcv = (TextView) findViewById(R.id.txtRecvData);
        textMessage =(TextView) findViewById(R.id.lbText);
        textATemp = (TextView) findViewById(R.id.txtATemp);
        textHumdy = (TextView) findViewById(R.id.txtHmdty);
        textConductivity = (TextView) findViewById(R.id.txtRawCndct);
        textTemperature = (TextView) findViewById(R.id.txtRawTemp);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onDestroy() {

        mAdkReadTask.cancel(true);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }
    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    public void BindServiceADK(View view){

        if (!mBound) {
            Toast.makeText(FullscreenActivity.this, "Not bind yet, start binding", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FullscreenActivity.this, LocalService.class);
           // startService(serviceIntent);
            bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
/*
        if (mBound) {
            readValue = mService.getADKread();
            Toast.makeText(FullscreenActivity.this, "readString: " + readValue, Toast.LENGTH_SHORT).show();
        }
 */
    }
    public void unBindServiceADK(View view){
        if (mBound) {
            unbindService(mConnection);
           // stopService(serviceIntent);
            mBound = false;
           Toast.makeText(FullscreenActivity.this, "Unbinded Service", Toast.LENGTH_SHORT).show();
        }
    }
    public void startRun(View view){
        //mAdkReadTask = new AdkReadTask();
        try{
         //   new AdkReadTask().execute();
            //AsyncTask..execute(); only can run 1 task, use thread_pool_executor can run 5 task after API19
            mAdkReadTask = new AdkReadTask();
            mAdkReadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
         //   mAdkWriteTask = new AdkWriteTask();
         //   mAdkWriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }catch (Exception ex){
            Toast.makeText(FullscreenActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //mAdkReadTask.execute();
       // mAdkWriteTask = new AdkWriteTask();
        //mAdkWriteTask.execute();
    }
    public void stopRun(View view){
        if (mAdkReadTask !=null){
            mAdkReadTask.pause();
        }
       // mAdkWriteTask.cancel(true);
       //mAdkWriteTask.cancel(true);
        //mAdkWriteTask.cancel(true);
    }

    private class AdkReadTask extends AsyncTask<Void, String, Void> {
        private boolean running = true;
        public void pause(){
            running = false;
        }
        protected Void doInBackground(Void... params) {
//	    	Log.i("ADK demo bi", "start adkreadtask");
            while(running) {
                publishProgress(mService.getADKread()) ;
                //publishProgress("TEST") ;
                //2016.11.23 calen.
                //if(isCancelled()) break;
            }
            return null;
        }
        protected void onProgressUpdate(String... progress) {
            readValue = progress[0];
            parseMsg(readValue);
            textRcv.setText(readValue);
            //distance.setText((int)progress[0].charAt(0) + " cm");
//	    	Log.i(TAG, "received: " + (int)progress[0].charAt(0));
        }
    }
    private class AdkWriteTask extends AsyncTask<Void, String, Void> {
      //  private boolean running = true;
       // public void pause(){
        //    running = false;
       // }
        protected Void doInBackground(Void... params) {
            //	    	Log.i("ADK demo bi", "start adkreadtask");
            //  while(running) {
            //String sendtxt = "ADKsendTEST";
            publishProgress(mService.getADKsend(sendtxt));

            //publishProgress("TEST") ;
            //2016.11.23 calen.
            //  if(isCancelled()) break;
            return null;
        }
            // return null;
            //}

        protected void onProgressUpdate(String... progress) {
            txt ="";
            txt = progress[0];
            //readValue = progress[0];
            //parseADK(readValue);
           // textRcv.setText(readValue);
            textMessage.setText(txt);
            //distance.setText((int)progress[0].charAt(0) + " cm");
//	    	Log.i(TAG, "received: " + (int)progress[0].charAt(0));
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
            mService = binder.getService();
           // mService.registerClient(FullscreenActivity.this);
            Toast.makeText(FullscreenActivity.this, "Service connected", Toast.LENGTH_SHORT).show();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Toast.makeText(FullscreenActivity.this, "Service disconnected", Toast.LENGTH_SHORT).show();
        }
    };

/*
   @Override
    public void updateClient(String readData) {

        seconds = (int) (millis / 1000) % 60 ;
        minutes = (int) ((millis / (1000*60)) % 60);
        hours   = (int) ((millis / (1000*60*60)) % 24);

    tvServiceOutput.setText((hours>0 ? String.format("%d:", hours) : "") + ((this.minutes<10 && this.hours > 0)? "0" + String.format("%d:", minutes) :  String.format("%d:", minutes)) + (this.seconds<10 ? "0" + this.seconds: this.seconds));
   }
*/

    private void parseMsg(String parseValue){
        if (parseValue != null){
            int MsgID = 99;
            int[] DI_Bit = new int[3];
            int[] DO_Bit = new int[3];
            double[] AI_Double = new double[4];
            double[] AO_Double = new double[4];

            //Log.d("ADK receive Value", readValue);
            JSONObject JObj_ADK = null;
            try {
                JObj_ADK = new JSONObject(parseValue);
                if (!JObj_ADK.isNull("MsgID")) {              //check MsgID  is existing or not 2017/06/04 Calen
                    MsgID = JObj_ADK.getInt("MsgID");
                }

                    switch (MsgID) {
                        case 1:
                        JSONArray arrDI = JObj_ADK.getJSONArray("DI_Bit");
                        //DI_bit  max:3
                        for (int i = 0; i < arrDI.length(); i++) {
                            DI_Bit[i] = arrDI.getInt(i);
                        }

                        JSONArray arrDO = JObj_ADK.getJSONArray("DO_Bit");
                        //DO_bit  max:3
                        for (int i = 0; i < arrDO.length(); i++) {
                            DO_Bit[i] = arrDO.getInt(i);
                        }

                        JSONArray arrAI = JObj_ADK.getJSONArray("AI_Double");
                        //AI_Double
                        for (int i = 0; i < arrAI.length(); i++) {
                            AI_Double[i] = arrAI.getDouble(i);
                        }

                        JSONArray arrAO = JObj_ADK.getJSONArray("AO_Double");
                        //AI_Double
                        for (int i = 0; i < arrAO.length(); i++) {
                            AO_Double[i] = arrAO.getDouble(i);
                        }

                        //   db_CompTemp = db_RawTemp
                            //
                            db_ATemp= AI_Double[0];
                            db_Hmdty= AI_Double[1];
                            db_RawCndct = AI_Double[2];
                            db_RawTemp =  AI_Double[3];


                            strATemp = String.format("%.2f", AI_Double[0]);
                        strHmdty = String.format("%.2f", AI_Double[1]);
                        strRawCndct = String.format("%.2f", AI_Double[2]);
                        strRawTemp = String.format("%.2f", AI_Double[3]);
                        //  strCompTemp = String.format("%.2f",db_CompTemp);
                        //   strCalCndct = String.format("%.2f",db_CalCndct);

                        //   //  strCalCl = String.format("%.2f",db_CalCl);
                        //    db_CompCl = db_CalCl;
                        //    strCompCl = String.format("%.2f",db_CompCl);

                        //JSONObject myjson = packUploadJSON(strATemp,strHmdty,strRawCndct,strCalCndct, strRawTemp, strCompTemp, strCalCl, strCompCl);
                        //mymesg = myjson.toString();
                        //reflashView(parseValue,strATemp,strHmdty,strRawCndct,strRawTemp,strCalCndct,strCalCl,mymesg);
                        case 3:
                            //Ard->And 3.1compensation 3.2 caluculation 3.3 system parameter


                        default:
                            //do something
                    }

            } catch (JSONException e) {
                Log.e("JSON_ADK Parser", "Error parsing data " + e.toString());
                strATemp = "0.00";
                strHmdty ="0.00";
                strRawCndct = "0.00";
                strRawTemp = "0.00";
                strCalCndct = "0.00";
                strCalCl = "0.00";
                strCompCl ="0.00";

                db_ATemp= 0.0;
                db_Hmdty= 0.0;
                db_RawCndct = 0.0;
                db_RawTemp =  0.0;
                // reflashView(parseValue,strATemp,strHmdty,strRawCndct,strRawTemp,strCalCndct,strCalCl,mymesg );
            }
        }else
        {
            Log.e("ADK_Read", "Error parsing data " + "result=0");
            strATemp = "0.00";
            strHmdty ="0.00";
            strRawCndct = "0.00";
            strRawTemp = "0.00";
            strCalCndct = "0.00";
            strCalCl = "0.00";
            strCompCl ="0.00";
            db_ATemp= 0.0;
            db_Hmdty= 0.0;
            db_RawCndct = 0.0;
            db_RawTemp =  0.0;
            // reflashView(parseValue,strATemp,strHmdty,strRawCndct,strRawTemp,strCalCndct,strCalCl,mymesg );
        }
        textATemp.setText(strATemp);
        textHumdy.setText(strHmdty);
        textConductivity.setText(strRawCndct);
        textTemperature.setText(strRawTemp);
    }



    private void parseADK(String parseValue){
        if (parseValue != null){
            //Log.d("ADK receive Value", readValue);
            JSONObject JObj_ADK = null;
            try {
                JObj_ADK = new JSONObject(parseValue);
                db_ATemp= JObj_ADK.getDouble("ATemp");
                db_Hmdty= JObj_ADK.getDouble("Hmdty");
                db_RawCndct = JObj_ADK.getDouble("RawCndct");
                db_RawTemp = JObj_ADK.getDouble("RawTemp");
                //db_RawCndct = JObj_ADK.getDouble("RawCndct");
                //int_RawCndct = JObj_ADK.getInt("RawCndct");
                //db_RawCndct = ADConverter(int_RawCndct,compParam[s_compCndct][s_compMin],compParam[s_compCndct][s_compMax]);
                //db_RawTemp = JObj_ADK.getDouble("RawTemp");
                //int_RawTemp = JObj_ADK.getInt("RawTemp");
                //db_RawTemp = ADConverter(int_RawTemp,compParam[s_compTemp][s_compMin],compParam[s_compTemp][s_compMax]);
                //db_RawTemp = ADConverter(int_RawTemp,0,100);
                //Calculate the Conductivity 20150601 Edited by Calen
                //db_Conductivity = JObj_ADK.getDouble("Conductivity");
                //db_Temperature = JObj_ADK.getDouble("Temperature");
                db_CompTemp = db_RawTemp;
                //calculateResult();

                //double dout=28.965432;
                //BigDecimal bd= new BigDecimal(dout);
                //bd=bd.setScale(4, BigDecimal.ROUND_HALF_UP);// 小數後面四位, 四捨五入
                //

                strATemp = String.format("%.2f",db_ATemp);
                strHmdty = String.format("%.2f",db_Hmdty);
                strRawCndct = String.format("%.2f",db_RawCndct);
                strRawTemp = String.format("%.2f",db_RawTemp);
                strCompTemp = String.format("%.2f",db_CompTemp);
                strCalCndct = String.format("%.2f",db_CalCndct);

                strCalCl = String.format("%.2f",db_CalCl);
                db_CompCl = db_CalCl;
                strCompCl = String.format("%.2f",db_CompCl);

                //JSONObject myjson = packUploadJSON(strATemp,strHmdty,strRawCndct,strCalCndct, strRawTemp, strCompTemp, strCalCl, strCompCl);
                //mymesg = myjson.toString();
                //reflashView(parseValue,strATemp,strHmdty,strRawCndct,strRawTemp,strCalCndct,strCalCl,mymesg);

            } catch (JSONException e) {
                Log.e("JSON_ADK Parser", "Error parsing data " + e.toString());
                strATemp = "0.00";
                strHmdty ="0.00";
                strRawCndct = "0.00";
                strRawTemp = "0.00";
                strCalCndct = "0.00";
                strCalCl = "0.00";
                strCompCl ="0.00";
               // reflashView(parseValue,strATemp,strHmdty,strRawCndct,strRawTemp,strCalCndct,strCalCl,mymesg );
            }
        }else
        {
            Log.e("ADK_Read", "Error parsing data " + "result=0");
            strATemp = "0.00";
            strHmdty ="0.00";
            strRawCndct = "0.00";
            strRawTemp = "0.00";
            strCalCndct = "0.00";
            strCalCl = "0.00";
            strCompCl ="0.00";
           // reflashView(parseValue,strATemp,strHmdty,strRawCndct,strRawTemp,strCalCndct,strCalCl,mymesg );
        }
    }

    public void Send2Arduino() {
        JSONObject json = packArduinoJSON();
       // mService.getADKsend(json.toString());

        //mAdkManager.writeSerial(json.toString());
    }
    public void sendMsg(View view){
        JSONObject json = new JSONObject();
        try {

            JSONArray arrDO = new JSONArray();
            arrDO.put(1);
            arrDO.put(0);
            arrDO.put(1);
            JSONArray arrAO = new JSONArray();
            Toast.makeText(this, String.valueOf(db_ATemp) +":" +String.valueOf(db_Hmdty), Toast.LENGTH_SHORT).show();
            arrAO.put(db_ATemp);
            arrAO.put(db_Hmdty);
           // arrAO.put(Math.random()* 10000);
           // arrAO.put(Math.random()* 10000);
            json.put("MsgID",2);
            json.put("DO_Bit",arrDO);
            json.put("AO_Double", arrAO);

          } catch (Exception e)
           { Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();}

          try {
              sendtxt = json.toString();
              AdkWriteTask writeTask = new AdkWriteTask();
              writeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
          }catch (Exception e) {
              Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
          }

        //  mAdkWriteTask = new AdkWriteTask();
        //  mAdkWriteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private JSONObject packArduinoJSON(){

        JSONObject json = new JSONObject();
        try {
            JSONArray Param1 = new JSONArray();
            //db_ATemp=0.0, db_Hmdty=0.0, db_RawCndct=0.0, db_Conductivity=0.0, db_RawTemp=0.0, db_Temperature=0.0;
            ParamA[0]=Math.random()* 1000;
            ParamA[1]=Math.random()* 1000;
            ParamA[2]=Math.random()* 1000;
            Param1.put(ParamA[0]);
            Param1.put(ParamA[1]);
            Param1.put(ParamA[2]);

            json.put("Param1", Param1);

        } catch (Exception e)
        {
        }

        return json;
    }


}
