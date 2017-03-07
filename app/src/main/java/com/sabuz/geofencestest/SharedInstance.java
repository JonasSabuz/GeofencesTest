package com.sabuz.geofencestest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

/**
 * Created by apple on 5/5/16.
 */
public class SharedInstance {

    private static final String TAG = SMConstants.LoggerPrefix + SharedInstance.class.getSimpleName();

    private static SharedInstance singleton = null;

    private Location currentDeviceLocation = null;

    private SharedInstance() {
    }

    public static SharedInstance getInstance() {

        if (singleton == null) {
            singleton = new SharedInstance();
        }
        return singleton;
    }

    public Location getCurrentDeviceLocation() {
//        if (Application.getGpsService() != null && Application.getGpsService().enabled()) {
//            currentDeviceLocation = Application.getGpsService().getLocation();
//        }
        if (currentDeviceLocation == null) {
            SharedPreferences prefs = Application.getContext().getSharedPreferences("location", Context.MODE_PRIVATE);
            String json = prefs.getString("location", null);
            Location loc = json == null ? null : new Gson().fromJson(json, Location.class);
            if (loc == null) {
                currentDeviceLocation = new Location("dummyprovider");
                currentDeviceLocation.setLatitude(SMConstants.defaultLatitude);
                currentDeviceLocation.setLongitude(SMConstants.defaultLongitude);
            }
            else
                currentDeviceLocation = loc;
        }

        return currentDeviceLocation;
    }

    public void setCurrentDeviceLocation(Location currentDeviceLocation) {
        if (currentDeviceLocation != null) {
            this.currentDeviceLocation = currentDeviceLocation;
            SharedPreferences prefs = Application.getContext().getSharedPreferences("location", Context.MODE_PRIVATE);
            String json = this.currentDeviceLocation == null ? null : new Gson().toJson(this.currentDeviceLocation);
            prefs.edit().putString("location", json).apply();
        }
    }
}
