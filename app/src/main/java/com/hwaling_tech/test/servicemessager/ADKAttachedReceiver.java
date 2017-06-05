package com.hwaling_tech.test.servicemessager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class ADKAttachedReceiver extends BroadcastReceiver {

    public ADKAttachedReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent ADKIntent = new Intent(context, LocalService.class);
        context.startService(ADKIntent);
    }

}
