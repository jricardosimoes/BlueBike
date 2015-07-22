/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ricardosimoes.bluebike;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.preference.PreferenceManager;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import static com.ricardosimoes.bluebike.SampleGattAttributes.*;
import static com.ricardosimoes.bluebike.TireAttributes.*;


import com.jjoe64.graphview.*;


import static com.ricardosimoes.bluebike.CSCData.*;
import java.sql.Date;
import java.sql.Timestamp;
import com.opencsv.CSVWriter;
import com.ricardosimoes.bluebike.CSCDataRow;



/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String RECORDINGS_FILE = "recordings.csv";

    public static final double KM_TO_MILES = 0.621371192;



    private Menu menu_ = null;

    private TextView mConnectionState;


    private TextView mDeviceNameField;

    private TextView mDataCadenceField;
    private TextView mDataCadenceMaxField;

    private TextView mDataCadenceAvgField;

    private TextView mDataSpeedField;
    private TextView mDataSpeedMaxField;

    private TextView mDataOdometerField;

    private TextView mDataSpeedAvgField;


    private TextView mDataRecordingStateField;

    private ProgressBar mDataBaterryProgress;

    private String mDeviceName;
    private String mDeviceAddress;


    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private boolean is_running = false;

    private int cumulative_wheel_revolutions = 0;
    private int last_wheel_event_time = 0;
    private int cumulative_crank_revolutions = 0;
    private int last_crank_event_time = 0;
    private int wheel_rpm = 0;
    private int crank_rpm = 0;

    private int last_wheel_event_time_ = 0;
    private int last_crank_event_time_ = 0;

    private int cumulative_wheel_revolutions_ = 0;
    private int cumulative_crank_revolutions_ = 0;
    private int wheel_revolutions_per_minute_ = 0;
    private int crank_revolutions_per_minute_ = 0;


    private int first_cumulative_wheel_revolutions = 0;
    private int first_cumulative_crank_revolutions = 0;



    private SharedPreferences preferences;


    private float wheel_len;


    private float speed_max = 0;
    //private float speed_min = 0;
    private float speed_avg = 0;


    private float cadence_max = 0;
    //private float cadence_min = 0;
    private float cadence_avg = 0;



    //private GraphView graphViewSpeed;
   // private GraphView graphViewCadence;

    //private GraphViewSeries cadenceSeries;
   // private GraphViewSeries speedSeries;

    private float xcadence = 0;
    private float xspeed = 0;


    private ArrayList<String> cadenceHistory = new ArrayList<String> ();
    private ArrayList<String> speedHistory = new ArrayList<String> ();


    String csvfile;
    java.sql.Timestamp startTimestamp;

    private String lenght_unit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.gatt_services_characteristics);
        getActionBar().setTitle(R.string.title_devices);
        setContentView(R.layout.gatt_service_csc);

        final Intent intent = getIntent();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        wheel_len = Float.valueOf(preferences.getString("pref_tire", "2289.22"));
        lenght_unit = preferences.getString("pref_units", "km");
        //Log.d(TAG, "pref_tire: " +  preferences.getString("pref_tire", ""));
        //Log.d(TAG, "pref_tire: " +  wheel_len);

        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);


        mDeviceNameField = (TextView) findViewById(R.id.device_name);
        mDataCadenceField = (TextView) findViewById(R.id.cadence);
        mDataSpeedField = (TextView) findViewById(R.id.speed);

        mDataCadenceMaxField= (TextView) findViewById(R.id.cadence_max);
        //mDataCadenceMinField= (TextView) findViewById(R.id.cadence_min);
        mDataCadenceAvgField= (TextView) findViewById(R.id.cadence_avg);

        mDataSpeedMaxField= (TextView) findViewById(R.id.speed_max);
        //mDataSpeedMinField= (TextView) findViewById(R.id.cadence_min);
        mDataSpeedAvgField= (TextView) findViewById(R.id.speed_avg);

        mDataBaterryProgress = (ProgressBar) findViewById(R.id.baterry_level);
        mDataOdometerField = (TextView) findViewById(R.id.odometer);

        mDataRecordingStateField = (TextView) findViewById(R.id.recording_state);

        mDeviceNameField.setText(mDeviceName);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_start_log).setVisible(true);
            menu.findItem(R.id.menu_stop_log).setVisible(false);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            menu.findItem(R.id.menu_start_log).setVisible(false);
            menu.findItem(R.id.menu_stop_log).setVisible(false);
        }
        menu_ = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                Log.d(TAG, "mDeviceAddress " + mDeviceAddress.toString());
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;

            case R.id.menu_config:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_recordings:
                final Intent intent3 = new Intent(this, RecordingsActivity.class);
                startActivity(intent3);
                break;
            case R.id.menu_about:
                final Intent intent2 = new Intent(this, AboutActivity.class);
                startActivity(intent2);
                break;

            case R.id.menu_start_log:
                startLogging();
                break;
            case R.id.menu_stop_log:
                stopLogging();
                break;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Menu getMenu()
    {
        return menu_;
    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
                displayGattServices(mBluetoothLeService);


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                int array[] = intent.getIntArrayExtra(BluetoothLeService.EXTRA_DATA_SPEED_CADENCE);
                //Log.d(TAG, String.format("Received data from intent: %d, %d, %d, %d",  array[0], array[1], array[2], array[3] ));
                if(!is_running){
                    is_running = true;
                    first_cumulative_crank_revolutions = array[2];
                    first_cumulative_wheel_revolutions = array[0];
                    //Calendar calendar = Calendar.getInstance();
                    //java.util.Date now = calendar.getTime();
                    //java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

                }else{
                    if(first_cumulative_crank_revolutions == 0)
                        first_cumulative_crank_revolutions = array[2];
                    if(first_cumulative_wheel_revolutions == 0)
                        first_cumulative_wheel_revolutions = array[0];
                }
                displayData(array);
            }
        }
    };




    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);

        mDataCadenceField.setText(R.string.no_data);
        mDataSpeedField.setText(R.string.no_data);

    }



    private void startLogging(){

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        startTimestamp = new java.sql.Timestamp(now.getTime());

        long unixTime = System.currentTimeMillis() / 1000L;
        csvfile = "CSC_" + String.valueOf(unixTime) + ".csv";

        menu_.findItem(R.id.menu_start_log).setVisible(false);
        menu_.findItem(R.id.menu_stop_log).setVisible(true);
        mDataRecordingStateField.setText(R.string.label_recording);
        Log.d(TAG, "Recording started to " + csvfile);

        Toast toast = Toast.makeText(getApplicationContext(), R.string.label_recording_started, Toast.LENGTH_SHORT);
        toast.show();


    }

    private void stopLogging(){

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        FileOutputStream outputStream;

        try {


            String newline =  startTimestamp.toString() + "\t"
                    + currentTimestamp.toString() + "\t"
                    + csvfile + "\t"
                    + wheel_len + "\t"
                    + speed_max + "\t"
                    + speed_avg + "\t"
                    + cadence_max + "\t"
                    + cadence_avg + "\t"
                    + mDataOdometerField.getText() + "\t"
                    + lenght_unit + "\t"
                    +"\n";

            outputStream = openFileOutput(RECORDINGS_FILE, Context.MODE_APPEND);
            outputStream.write(newline.getBytes());
            outputStream.flush();

            outputStream.close();

            menu_.findItem(R.id.menu_start_log).setVisible(true);
            menu_.findItem(R.id.menu_stop_log).setVisible(false);
            mDataRecordingStateField.setText("");
            Log.d(TAG, "Recording stoped to " + csvfile);
            Toast toast = Toast.makeText(getApplicationContext(), R.string.label_recording_stoped, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();

            Toast toast = Toast.makeText(getApplicationContext(), R.string.label_recording_impossible, Toast.LENGTH_LONG);
            toast.show();

        }


    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            //mDataField.setText(data);
        }
    }

    private void displayData(int array[]) {



        cumulative_wheel_revolutions = array[0];
        last_wheel_event_time = array[1];
        cumulative_crank_revolutions = array[2];
        last_crank_event_time = array[3];

        int cumulative_wheel_revolutions_since_start = 0;


        if(last_wheel_event_time_ > 0) {
            //Log.d(TAG, "");
            //Log.d(TAG, String.format("calculate the wheel RPM: %d, %d, %d, %d", cumulative_wheel_revolutions, last_wheel_event_time, cumulative_wheel_revolutions_, last_wheel_event_time_) );
            // now calculate the wheel RPM value assuming we have more than one data point available
            int event_time_delta = 0;
            int wheel_revs_delta = 0;

            if (last_wheel_event_time >= last_wheel_event_time_) {
                event_time_delta = last_wheel_event_time - last_wheel_event_time_;
            } else {
                event_time_delta = 65536 - last_wheel_event_time_ + last_wheel_event_time;
            }
            wheel_revs_delta = cumulative_wheel_revolutions - cumulative_wheel_revolutions_;
            double event_time_s = event_time_delta / 1024.0;
            double event_time_delta_ms = event_time_s * 1000;
            if ((event_time_delta_ms * wheel_revs_delta) > 0) {
                double rpm_real = (60000.0 / event_time_delta_ms) * wheel_revs_delta;
                wheel_rpm = (int) rpm_real;
            } else {
                wheel_rpm = wheel_revolutions_per_minute_;
            }


            cumulative_wheel_revolutions_since_start = cumulative_wheel_revolutions - first_cumulative_wheel_revolutions;

        }

        if(last_crank_event_time_ > 0) {
            //Log.d(TAG, String.format("calculate the crank RPM: %d, %d, %d, %d", cumulative_crank_revolutions, last_crank_event_time, cumulative_crank_revolutions_, last_crank_event_time_) );
            // now calculate the crank RPM value assuming we have more than one data point available
            int event_time_delta = 0;
            int crank_revs_delta = 0;
            if (last_crank_event_time >= last_crank_event_time_) {
                event_time_delta = last_crank_event_time - last_crank_event_time_;
            } else {
                event_time_delta = 65536 - last_crank_event_time_ + last_crank_event_time;
            }
            double event_time_s = event_time_delta / 1024.0;
            double event_time_delta_ms = event_time_s * 1000;
            crank_revs_delta = cumulative_crank_revolutions - cumulative_crank_revolutions_;
            if ((event_time_delta_ms * crank_revs_delta) > 0) {
                double rpm_real = (60000.0 / event_time_delta_ms) * crank_revs_delta;
                crank_rpm = (int) rpm_real;
            } else {
                crank_rpm = crank_revolutions_per_minute_;
            }
        }


        float speed = (wheel_rpm * wheel_len)/1000/60;
        float odometer = cumulative_wheel_revolutions_since_start* wheel_len /1000/1000;


        switch (lenght_unit){
            case "mi":
                speed = speed/(float)KM_TO_MILES;
                odometer =  odometer/(float)KM_TO_MILES;
                break;
            case "m":
                speed = speed * 1000;
                odometer =  odometer * 1000;
                break;
        }


        mDataOdometerField.setText(String.format("%.2f %s", odometer, lenght_unit));

        last_crank_event_time_ = last_crank_event_time;
        last_wheel_event_time_ = last_wheel_event_time;
        cumulative_wheel_revolutions_ = cumulative_wheel_revolutions;
        cumulative_crank_revolutions_ = cumulative_crank_revolutions;

        if(speed > speed_max){
            speed_max = speed;
        }
        speed_avg = (speed_avg + speed)/2;



        mDataSpeedField.setText(String.format("%.1f %s/h", speed, lenght_unit));
        mDataSpeedMaxField.setText(String.format("%.1f", speed_max));
        mDataSpeedAvgField.setText(String.format("%.1f", speed_avg));


        if(crank_rpm > cadence_max){
            cadence_max = crank_rpm;
        }
        cadence_avg = (cadence_avg + crank_rpm)/2;

        mDataCadenceField.setText(String.valueOf(crank_rpm) + " rpm");
        mDataCadenceMaxField.setText(String.format("%.0f", cadence_max));
        mDataCadenceAvgField.setText(String.format("%.0f", cadence_avg));


        Log.d(TAG, String.format("speed %.1f, speed_max %.1f, speed_avg %.1f, crank_rpm %d, cadence_max %.0f, cadence_avg %.0f", speed, speed_max, speed_avg, crank_rpm, cadence_max, cadence_avg));

        cadenceHistory.add(String.valueOf(crank_rpm));
        speedHistory.add(String.valueOf((int)speed));
        if(cadenceHistory.size() == 45){
            cadenceHistory.remove(0);
        }

        if(speedHistory.size() == 45){
            speedHistory.remove(0);
        }



        graphData(cadenceHistory, speedHistory);
        if(menu_.findItem(R.id.menu_stop_log).isVisible())
            logData(speed, crank_rpm);


    }

    private void logData(float speed, int cadence){
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        FileOutputStream outputStream;
        String newline = currentTimestamp.toString() + "\t" + String.valueOf(speed) + "\t" + String.valueOf(cadence) + "\n";

        try {
            outputStream = openFileOutput(csvfile, Context.MODE_APPEND);
            outputStream.write(newline.getBytes());
            outputStream.flush();

            outputStream.close();

            Log.d(TAG, csvfile + ": " + newline);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }



    private void graphData(ArrayList<String> cadenceHistory, ArrayList<String> speedHistory){

        LinearLayout layoutS = (LinearLayout) findViewById(R.id.speedXYPlot);
        LinearLayout layoutC = (LinearLayout) findViewById(R.id.cadenceXYPlot);

        int num = 45;
        GraphView.GraphViewData[] datac = new GraphView.GraphViewData[num];
        Iterator<String> itc = cadenceHistory.iterator();
        int i = 0;
        double[] dataca = new double[num];
        while(itc.hasNext()) {
            Object obj = itc.next();
            dataca[i] = Double.valueOf(obj.toString());
            //Log.d(TAG, String.valueOf(dataca[i]));
            i++;
        }

        //Log.d(TAG, dataca.toString());

        for (i=0; i<num; i++) {
            datac[i] = new GraphView.GraphViewData(i, dataca[i]);
        }

        GraphView graphViewCadence  = new LineGraphView(this, getString(R.string.label_cadence));

        graphViewCadence.addSeries(new GraphViewSeries(datac));

        graphViewCadence.setViewPort(2, 40);
        graphViewCadence.setScrollable(true);
        graphViewCadence.setScalable(true);
        graphViewCadence.setShowHorizontalLabels(false);

        layoutC.removeAllViews();
        layoutC.addView(graphViewCadence);



        GraphView.GraphViewData[] datas = new GraphView.GraphViewData[num];
        Iterator<String> its = speedHistory.iterator();
        i = 0;



        double[] datasa = new double[45];
        while(its.hasNext()) {
            Object obj = its.next();
            datasa[i] = Double.valueOf(obj.toString());
            //Log.d(TAG, String.valueOf(datasa[i]));
            i++;
        }

        for (i=0; i<num; i++) {
            datas[i] = new GraphView.GraphViewData(i, datasa[i]);
        }



        GraphView graphViewSpeed  = new LineGraphView(this, getString(R.string.label_speed));

        graphViewSpeed.addSeries(new GraphViewSeries(datas));
        graphViewSpeed.setShowHorizontalLabels(false);
        graphViewSpeed.setViewPort(2, 40);
        graphViewSpeed.setScrollable(true);
        graphViewSpeed.setScalable(true);

        layoutS.removeAllViews();
        layoutS.addView(graphViewSpeed);


    }



    private void displayData(String data, TextView dataField) {
        if (data != null) {
            dataField.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(BluetoothLeService mBluetoothLeService) {

        List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            Log.d(TAG, "Service: " + uuid);
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                Log.d(TAG, "    Characteristic: " + uuid);

                if(gattService.getUuid().toString().equals(CYCLING_SPEED_AND_CADENCE)){
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                        Log.d(TAG, "mBluetoothLeService.setCharacteristicNotification: CYCLING_SPEED_AND_CADENCE " + uuid);

                }

                if(gattService.getUuid().toString().equals(BATTERY_SERVICE)){
                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    Log.d(TAG, "mBluetoothLeService.setCharacteristicNotification: BATTERY_SERVICE" + uuid);
                }

            }


            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);




        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
