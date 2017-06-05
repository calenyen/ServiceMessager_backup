package com.hwaling_tech.test.servicemessager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
       //marked by calen 2016.07.27
        Intent bootIntent = new Intent(context,FullscreenActivity.class);
        context.startActivity(bootIntent);
    }
    


}