package com.sabuz.geofencestest;

import android.text.format.DateUtils;

/**
 * Created by apple on 5/9/16.
 */
public class SMConstants {

    public static final String LoggerPrefix = "SWM_";
    public static final String BLEDeviceTAG = SMConstants.LoggerPrefix + "BLEDevice";

    public static final String CURRENT_FW_VERSION = "SMv2.32.3";

    public static final boolean ENABLE_DFU = false;
    public static final long WH_UPDATE_PERIOD = DateUtils.DAY_IN_MILLIS / 4; // 6 hours

    /*configuration for foreground scan parameters*/
    public static final int TIME_WINDOW_FOR_CHECKING_DEVICE_RANGE = 2000; // origin: 1000
    public static final long BACKGROUND_SCAN_IDLE_PERIOD_ALL_WH_TRIGGERED = 15000;
    public static final long BACKGROUND_SCAN_IDLE_PERIOD = 7500; //origin: 5000
    public static final long FOREGROUND_SCAN_IDLE_PERIOD = 2000;
    public static final long FOREGROUND_SCAN_PERIOD = 5000;
    public static final long BACKGROUND_SCAN_PERIOD = 5000;
    public static final long SCAN_PERIOD = 5000;
    public static final long MAPTIME_TIME_OUT_WINDOW = 12000;

    public static final int MAX_GPS_RANGE_METERS = 1000;

    public static final long DEFAULT_DEVICE_TIME_OUT = 75 * 1000; // 1 minute 15 seconds

    public static final long SCAN_TIME_4 = 5000;
    public static final long SCAN_TIME_5 = 4000;
    public static final long SCAN_TIME_6 = 2000;
    public static final int Welcome_Window_Safe_Bound = 2; // mins

    public static final int DEVICE_NAME_MAX_LENGTH = 19;
    public static final int DEVICE_NAME_MIN_LENGTH = 3;

    public static final int TIMER_RESERVED_ID = 0;
    public static final int TIMER_COUNT = 2;

    public static final int SEEN_COUNT = 6;
    public static final int WELCOME_RETRIES = 3;

    public static final long TIME_UPDATE_PERIOD = DateUtils.DAY_IN_MILLIS;
    public static final long RESET_CHECK_PERIOD = 60000;
    public static final long LAST_ACTION_PERIOD = 60000;

    public static final long HOUR = 60 * 60 * 1000;

    public static final String supportEmail = "somebody@switchmate.com";

    public enum BLEConnectionState {

        STATE_DISCONNECTED("STATE_DISCONNECTED"),
        STATE_CONNECTING("STATE_CONNECTING"),
        STATE_CONNECTED("STATE_CONNECTED"),
        STATE_DISCONNECTING("STATE_DISCONNECTING"),
        STATE_OPERATION_SUCCESS("STATE_OPERATION_SUCCESS"),
        STATE_UNKNOWN("STATE_UNKNOWN");

        private String value;

        private BLEConnectionState(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

    }
/*
* For welcome settings.
* */

    public static final String SETTINGS = "SETTINGS";
    public static final String DEVICES = "DEVICES";
    public static final String SharedPrefName = "SharedPref";
    public static final String SharedPref_Welcome_alert_key = "isWelcomePopupEnable";
    public static final String SharePref_Welcome_alert_time_label = "autoOnTime";

    public static final int[] WELCOME_START_TIME = new int[]{21, 30};
    public static final int[] WELCOME_STOP_TIME = new int[]{6, 30};
    public static final double defaultLatitude = 37.3255639;
    public static final double defaultLongitude = -121.9712313;
    public static final int AUTO_TURN_OFF_TIME = 30; //mins
    public static final int AUTO_TURN_OFF_TIME_GARAGE_PORCH = 15;
    public static final int NOTIFY_USER_WELCOME_FEATURE_ON_BEFORE_WINDOW = 30; //30 mins before Welcome window

    public enum SwitchLocationOutOfBox {

        LivingRoom("LivingRoom"),
        Porch("Porch"),
        BackYard("BackYard"),
        Bedroom("Bedroom"),
        Kitchen("Kitchen"),
        DiningRoom("DiningRoom"),
        Garage("Garage"),
        Hallway("Hallway"),
        Bathroom("Bathroom"),
        FamilyRoom("FamilyRoom"),
        Other("Other");

        private String value;

        private SwitchLocationOutOfBox(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
