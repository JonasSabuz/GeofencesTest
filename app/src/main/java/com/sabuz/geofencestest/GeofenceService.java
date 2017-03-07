package com.sabuz.geofencestest;

/**
 * Created by denys on 01.11.16.
 */

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GeofenceService extends IntentService {

    protected static final String TAG = SMConstants.LoggerPrefix + GeofenceService.class.getSimpleName();

    public static final String UPDATE_AFTER_REBOOT_ACTION = "com.test.UPDATE_AFTER_REBOOT";
    public static final String CONNECT_GOOGLE_API = "com.test.CONNECT_GOOGLE_API";
    public static final String DISCONNECT_GOOGLE_API = "com.test.DISCONNECT_GOOGLE_API";

    public static final String REMOVE_GEOFANCES = "com.test.REMOVE_GEOFANCES";

    private static boolean isGoogleApiConnected = false;

    public GeofenceService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent == null) {
            Log.e(TAG, "onHandleIntent, intent = null!!!");
        }

        Log.i(TAG, "onHandleIntent");

        if (UPDATE_AFTER_REBOOT_ACTION.equals(intent.getAction())) {
            Log.i(TAG, "updateGeofences action");
            updateGeofences();
            return;
        } else if (CONNECT_GOOGLE_API.equals(intent.getAction())) {
            Log.i(TAG, "Connect to google api action");
            connectGoogleAPI();
            return;
        } else if (DISCONNECT_GOOGLE_API.equals(intent.getAction())) {
            Log.i(TAG, "Disconnect from google api action");
            disconnectGoogleAPI();
            return;
        } else  if (REMOVE_GEOFANCES.equals(intent.getAction())) {
            Log.i(TAG, "remove geofences action");
            removeGeofences();
            return;
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, "onHandleIntent: " + errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        StringBuilder sb = new StringBuilder("");
        boolean showNotification = false;

        Log.i(TAG, getTransitionString(geofenceTransition));

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence g : triggeringGeofences) {
                String id=g.getRequestId();
                sb.append(id+" exit, ");
            }

        }
        else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {


            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence g : triggeringGeofences) {
                String id=g.getRequestId();
                sb.append(id+" enter, ");
            }


            if (mLastLocation != null) {

                SharedInstance.getInstance().setCurrentDeviceLocation(mLastLocation);

            }


        }

        sendNotification(getApplicationContext(),sb.toString());
    }

    private static void sendNotification(Context context, String notification) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Geofences")
                .setContentText(notification);

        builder.setAutoCancel(false);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify((int)System.currentTimeMillis(), builder.build());
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Geofence entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Geofence exited";
            default:
                return "Geofence unknown";
        }
    }

    public static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many geofence points";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many geofence pending intents";
            default:
                return "Unknown geofence error";
        }
    }

    private void updateGeofences() {
        connectGoogleAPI();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
        }

        Log.d(TAG,"updateGeofences mLastLocation "+(mLastLocation!=null));
        if(mLastLocation!=null) {

            addDeviceForGeofence(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            sendNotification(getApplicationContext(),"mLastLocation ("+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+")");
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
        }

        disconnectGoogleAPI();
        sendNotification(getApplicationContext(),"start");
    }

    private void removeGeofences()
    {
        connectGoogleAPI();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
        }

        removeDeviceFromGeofence();
        sendNotification(getApplicationContext(),"remove all");

        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
        }

        disconnectGoogleAPI();
        sendNotification(getApplicationContext(),"end");
    }

    private static GoogleApiClient mGoogleApiClient;
    private static PendingIntent mGeofencePendingIntent;
    private static Location mLastLocation;
    private static boolean isLocationEnabled = false;
    private static LocationRequest mLocationRequest;

    public static Location getLastLocation() {
        return mLastLocation;
    }

    private static GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient == null)
            buildGoogleApiClient();

        return mGoogleApiClient;
    }

    public static void connectGoogleAPI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Application.getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    Application.getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "No permissions for location!!!");
                return;
            }
        }

        getGoogleApiClient();

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    public static void disconnectGoogleAPI() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, mLocationListener);
            } catch (Exception e) {
                Log.e(TAG, "removeLocationUpdates", e);
            }

            isGoogleApiConnected = false;

            mGoogleApiClient.disconnect();

            Log.i(TAG, "Disconnected from GoogleApiClient");
        }
    }

    public static boolean isLocationEnabled() {
        LocationManager lm = (LocationManager)
                Application.getContext().getSystemService(Context.LOCATION_SERVICE);
        String provider = lm.getBestProvider(new Criteria(), true);
        return (StringUtils.isNotBlank(provider) &&
                !LocationManager.PASSIVE_PROVIDER.equals(provider));
    }

    private static LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            mLastLocation = location;
            SharedInstance.getInstance().setCurrentDeviceLocation(mLastLocation);
            Log.i(TAG, "new location: " + mLastLocation);
        }
    };

    private static void startLocationUpdates() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 60 * 1000);
        mLocationRequest.setFastestInterval(2 * 60 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS: {
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.i(TAG, "LocationSettingsStatusCodes.SUCCESS");
                        isLocationEnabled = true;

                        try {
                            if (mGoogleApiClient.isConnected())
                                LocationServices.FusedLocationApi.requestLocationUpdates(
                                        mGoogleApiClient, mLocationRequest, mLocationListener);
                        } catch (SecurityException securityException) {
                            logSecurityException(securityException);
                        } catch (IllegalStateException stateException) {
                            Log.i(TAG, "onResult: ", stateException);
                        }

                        break;
                    }

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                        // Location settings are not satisfied, but this can be fixed
//                        try {
//                            status.startResolutionForResult(Application.getCurrentActivity(),
//                                    0x1111);
//                        } catch (IntentSender.SendIntentException e) {
//                            e.printStackTrace();
//                        }
                        Log.i(TAG, "LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
                        isLocationEnabled = false;
                        break;
                    }

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog
                        Log.i(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                        isLocationEnabled = false;
                        break;
                    }
                }
            }
        });

    }

    private static synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(Application.getContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i(TAG, "Connected to GoogleApiClient");

                        isGoogleApiConnected = true;

                        try {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            SharedInstance.getInstance().setCurrentDeviceLocation(mLastLocation);
                            Log.i(TAG, "Last Location: " + mLastLocation);
                            startLocationUpdates();
                        }
                        catch (SecurityException securityException)
                        {
                            logSecurityException(securityException);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "Connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }

    private static PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(Application.getContext(), GeofenceService.class);
        mGeofencePendingIntent = PendingIntent.getService(Application.getContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }


    static final String[] RequestId=new String[]{"5m","10m","20m","30m","50m"};
    static final int[] Ranges=new int[]{5,10,20,30,50};

    public static synchronized void addDeviceForGeofence(double latitude,double longitude) {

        SharedPreferences sp = Application.getContext().getSharedPreferences(
                Application.getContext().getPackageName(),Context.MODE_PRIVATE);

        int period=sp.getInt("NotificationResponsiveness",10);

        List<Geofence> mGeofenceList = new ArrayList<>();

        for(int i=0;i<RequestId.length;i++) {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(RequestId[i])

                    .setCircularRegion(
                            latitude,
                            longitude,
                            Ranges[i]
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setNotificationResponsiveness(period*1000)
                    .build());
        }

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);

        try {

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    builder.build(),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "addGeofence successful ");
                    } else {
                        String errorMessage = getErrorString(status.getStatusCode());
                        Log.e(TAG, "addDevice:onResult: " + errorMessage);
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    public static synchronized void removeDeviceFromGeofence() {
        if (!getGoogleApiClient().isConnected()) {
            Log.e(TAG, "GoogleApiClient no yet connected. Try again.");

            return;
        }

        List<String> removeList = new ArrayList<>();
        for(int i=0;i<RequestId.length;i++)
            removeList.add(RequestId[i]);

        try {
            Log.i("TAG", "Trying to remove geofence ");
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    removeList
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.i(TAG, "removeGeofence successful");
                    } else {
                        String errorMessage = getErrorString(status.getStatusCode());
                        Log.e(TAG, "removeDevice:onResult: " + errorMessage);
                    }
                }
            });
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    private static void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    private static void showToast(String message) {
        Toast.makeText(Application.getContext(), message, Toast.LENGTH_LONG).show();
    }
}
