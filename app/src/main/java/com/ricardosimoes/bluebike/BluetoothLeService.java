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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    public final static String ACTION_GATT_CONNECTED =
            "com.ricardosimoes.bluebike.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.ricardosimoes.bluebike.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.ricardosimoes.bluebike.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.ricardosimoes.bluebike.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.ricardosimoes.bluebike.EXTRA_DATA";

    public final static String DATA_TYPE =
            "com.ricardosimoes.bluebike.DATA_TYPE";
    public final static String EXTRA_DATA_SPEED_CADENCE =
            "com.ricardosimoes.bluebike.EXTRA_DATA_SPEED_CADENCE";
    public final static String EXTRA_DATA_CADENCE =
            "com.ricardosimoes.bluebike.EXTRA_DATA_CADENCE";
    public final static String EXTRA_DATA_BATERRY_LEVEL =
            "com.ricardosimoes.bluebike.EXTRA_DATA_BATERRY_LEVEL";
    public final static String EXTRA_DATA_SPEED =
            "com.ricardosimoes.bluebike.EXTRA_DATA_SPEED";


    public final static UUID UUID_DEVICE_INFORMATION =
            UUID.fromString(SampleGattAttributes.DEVICE_INFORMATION);
    public final static UUID UUID_SERIAL_NUMBER_STRING =
            UUID.fromString(SampleGattAttributes.SERIAL_NUMBER_STRING);
    public final static UUID UUID_HARDWARE_REVISION_STRING =
            UUID.fromString(SampleGattAttributes.HARDWARE_REVISION_STRING);
    public final static UUID UUID_FIRMWARE_REVISION_STRING =
            UUID.fromString(SampleGattAttributes.FIRMWARE_REVISION_STRING);
    public final static UUID UUID_SOFTWARE_REVISION_STRING =
            UUID.fromString(SampleGattAttributes.SOFTWARE_REVISION_STRING);
    public final static UUID UUID_MANUFACTURER_NAME_STRING =
            UUID.fromString(SampleGattAttributes.MANUFACTURER_NAME_STRING);
    public final static UUID UUID_GENERIC_ACCESS_SERVICE =
            UUID.fromString(SampleGattAttributes.GENERIC_ACCESS_SERVICE);
    public final static UUID UUID_DEVICE_NAME =
            UUID.fromString(SampleGattAttributes.DEVICE_NAME);
    public final static UUID UUID_BATTERY_SERVICE =
            UUID.fromString(SampleGattAttributes.BATTERY_SERVICE);
    public final static UUID UUID_BATTERY_LEVEL =
            UUID.fromString(SampleGattAttributes.BATTERY_LEVEL);
    public final static UUID UUID_CYCLING_SPEED_AND_CADENCE =
            UUID.fromString(SampleGattAttributes.CYCLING_SPEED_AND_CADENCE);
    public final static UUID UUID_CSC_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.CSC_MEASUREMENT);
    public final static UUID UUID_CSC_FEATURE =
            UUID.fromString(SampleGattAttributes.CSC_FEATURE);
    public final static UUID UUID_SENSOR_LOCATION =
            UUID.fromString(SampleGattAttributes.SENSOR_LOCATION);
    public final static UUID UUID_SC_CONTROL_POINT =
            UUID.fromString(SampleGattAttributes.SC_CONTROL_POINT);


    private static final int DATA_SPEED_LEN = 7;
    private static final int DATA_CADENCE_LEN = 5;
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    static int WHEEL_REVOLUTION_DATA_PRESENT = 1; // 0
    static int CRANK_REVOLUTION_DATA_PRESENT = 2; // 1
    private final IBinder mBinder = new LocalBinder();
    private int cumulative_wheel_revolutions = 0;
    private int last_wheel_event_time = 0;
    private int cumulative_crank_revolutions = 0;
    private int last_crank_event_time = 0;

    private int last_wheel_event_time_ = 0;
    private int last_crank_event_time_ = 0;
    private int cumulative_wheel_revolutions_ = 0;
    private int cumulative_crank_revolutions_ = 0;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);


        if (UUID_CSC_MEASUREMENT.equals(characteristic.getUuid())) {

            int index = 0;
            boolean wheel_revolution_data_present = false; // bit 0 of Flags field
            boolean crank_revolution_data_present = false; // bit 1 of Flags field
            int flag = characteristic.getProperties();
            final byte[] data = characteristic.getValue();
            final byte[] val = data;
            if (data != null && data.length > 0) {


                byte flags = val[index];
                wheel_revolution_data_present = ((flags & WHEEL_REVOLUTION_DATA_PRESENT) == WHEEL_REVOLUTION_DATA_PRESENT);
                crank_revolution_data_present = ((flags & CRANK_REVOLUTION_DATA_PRESENT) == CRANK_REVOLUTION_DATA_PRESENT);
                index++;

                //Log.d(TAG, "wheel_revolution_data_present " + wheel_revolution_data_present);
                //Log.d(TAG, "crank_revolution_data_present " + crank_revolution_data_present);




                if (wheel_revolution_data_present) {

                    cumulative_wheel_revolutions = GattUtils.getIntValue(val, GattUtils.FORMAT_UINT32, index);
                    //Log.d(TAG, "GattUtils.getIntValue cumulative_wheel_revolutions " + cumulative_wheel_revolutions);

                    index = index + 4;

                    last_wheel_event_time = GattUtils.getIntValue(val, GattUtils.FORMAT_UINT16, index);

                    //Log.d(TAG, "GattUtils.getIntValue last_wheel_event_time " + last_wheel_event_time);

                    index = index + 2;


                }else{
                    last_wheel_event_time = last_wheel_event_time_;
                    cumulative_wheel_revolutions = cumulative_wheel_revolutions_;
                }

                if (crank_revolution_data_present) {

                    cumulative_crank_revolutions = GattUtils.getIntValue(val, GattUtils.FORMAT_UINT16, index);
                    //Log.d(TAG, "GattUtils.getIntValue cumulative_crank_revolutions" + cumulative_crank_revolutions);

                    index = index + 2;

                    last_crank_event_time = GattUtils.getIntValue(val, GattUtils.FORMAT_UINT16, index);
                    //Log.d(TAG, "GattUtils.getIntValue last_crank_event_time " + last_crank_event_time);

                    index = index + 2;


                }else{
                    last_crank_event_time = last_crank_event_time_;
                    cumulative_crank_revolutions = cumulative_crank_revolutions_;
                }


                int array[] = {cumulative_wheel_revolutions, last_wheel_event_time, cumulative_crank_revolutions, last_crank_event_time};

                Log.d(TAG, String.format("cumulative_wheel_revolutions %d, last_wheel_event_time %d, cumulative_crank_revolutions %d, last_crank_event_time %d", cumulative_wheel_revolutions, last_wheel_event_time, cumulative_crank_revolutions, last_crank_event_time));

                intent.putExtra(EXTRA_DATA_SPEED_CADENCE, array);


                last_crank_event_time_ = last_crank_event_time;
                last_wheel_event_time_ = last_wheel_event_time;
                cumulative_wheel_revolutions_ = cumulative_wheel_revolutions;
                cumulative_crank_revolutions_ = cumulative_crank_revolutions;


            }


        } else {            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(DATA_TYPE, EXTRA_DATA);
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


        if (UUID_BATTERY_LEVEL.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

        if (UUID_CSC_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }


    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
