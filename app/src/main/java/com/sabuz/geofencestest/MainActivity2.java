package com.sabuz.geofencestest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Map;

/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 *
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 *
 * Note that this Activity implements ResultCallback<Status>, requiring that
 * {@code onResult} must be defined. The {@code onResult} runs when the result of calling
 * {@link GeofencingApi#addGeofences(GoogleApiClient, GeofencingRequest, PendingIntent)}  addGeofences()} or
 * {@link GeofencingApi#removeGeofences(GoogleApiClient, java.util.List)}  removeGeofences()}
 * becomes available.
 */
public class MainActivity2 extends Activity {

    protected static final String TAG = "MainActivity";

    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;

    private boolean accessGranted=false;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private boolean mGeofencesAdded;

    private EditText et;

    private SharedPreferences sp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        // Get the UI widgets.
        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);

        et=(EditText)findViewById(R.id.et);
        sp=this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int period=sp.getInt("NotificationResponsiveness",10);
        et.setText(String.valueOf(period));
    }

    private void requestPermissions() {
        accessGranted = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*||
                    this.checkSelfPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) != PackageManager.PERMISSION_GRANTED*/ ) {
                accessGranted = false;
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("This app needs location access");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            } else {
                accessGranted = true;
            }
        } else {
            accessGranted = true;
        }
    }


    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler(View view) {

        String str=et.getText().toString().trim();
        int period=sp.getInt("NotificationResponsiveness",10);
        if(str.isEmpty())
        {
            et.setText("10");
        }
        else
        {
            period=Integer.parseInt(str);
            sp.edit().putInt("NotificationResponsiveness",period).commit();
        }

        Intent gIntent = new Intent(this, GeofenceService.class);
        gIntent.setAction(GeofenceService.UPDATE_AFTER_REBOOT_ACTION);
        startService(gIntent);
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler(View view) {
        Intent gIntent = new Intent(this, GeofenceService.class);
        gIntent.setAction(GeofenceService.REMOVE_GEOFANCES);
        startService(gIntent);
    }


    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
    private void setButtonsEnabledState() {
        if (mGeofencesAdded) {
            mAddGeofencesButton.setEnabled(false);
            mRemoveGeofencesButton.setEnabled(true);
        } else {
            mAddGeofencesButton.setEnabled(true);
            mRemoveGeofencesButton.setEnabled(false);
        }
    }
}