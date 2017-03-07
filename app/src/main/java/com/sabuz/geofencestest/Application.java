package com.sabuz.geofencestest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Application extends android.app.Application {

    private static final String TAG = SMConstants.LoggerPrefix + Application.class.getSimpleName();


    private Context mSharedContext = null;

    private static Application app;


    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

    }

    public synchronized static Context getSharedContext() {
        if (app.mSharedContext == null) {
            if (app.getSharedContext() != null) {
                return app.getSharedContext().getApplicationContext();
            }
        }

        return app.mSharedContext;
    }

    public static Context getContext() {
        return app;
    }

}