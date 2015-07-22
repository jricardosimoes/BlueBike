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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String DEVICE_INFORMATION         = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String SERIAL_NUMBER_STRING       = "00002a25-0000-1000-8000-00805f9b34fb";
    public static String HARDWARE_REVISION_STRING   = "00002a27-0000-1000-8000-00805f9b34fb";
    public static String FIRMWARE_REVISION_STRING   = "00002a26-0000-1000-8000-00805f9b34fb";
    public static String SOFTWARE_REVISION_STRING   = "00002a28-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME_STRING   = "00002a29-0000-1000-8000-00805f9b34fb";

    public static String GENERIC_ACCESS_SERVICE     = "00001800-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_NAME                = "00002a00-0000-1000-8000-00805f9b34fb";

    public static String BATTERY_SERVICE            = "00000180f-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_LEVEL              = "000002a19-0000-1000-8000-00805f9b34fb";

    public static String CYCLING_SPEED_AND_CADENCE  = "00001816-0000-1000-8000-00805f9b34fb";
    public static String CSC_MEASUREMENT            = "00002a5b-0000-1000-8000-00805f9b34fb";
    public static String CSC_FEATURE                = "00002a5c-0000-1000-8000-00805f9b34fb";
    public static String SENSOR_LOCATION            = "00002a5d-0000-1000-8000-00805f9b34fb";
    public static String SC_CONTROL_POINT           = "00002a55-0000-1000-8000-00805f9b34fb";


    static {

        attributes.put(DEVICE_INFORMATION , "Device Information Service");
        attributes.put(SERIAL_NUMBER_STRING, "Serial number string");
        attributes.put(HARDWARE_REVISION_STRING, "Hardware revision string");
        attributes.put(FIRMWARE_REVISION_STRING, "Firmware revision string");
        attributes.put(SOFTWARE_REVISION_STRING, "Software revision string");
        attributes.put(MANUFACTURER_NAME_STRING, "Manufacturer name string");

        attributes.put(BATTERY_SERVICE, "Battery Service");
        attributes.put(BATTERY_LEVEL, "Battery level");

        attributes.put(GENERIC_ACCESS_SERVICE , "Generic access service");
        attributes.put(DEVICE_NAME, "Device name");

        attributes.put(CYCLING_SPEED_AND_CADENCE, "Cycling Speed and Cadence");
        attributes.put(CSC_MEASUREMENT , "CSC measurement");
        attributes.put(CSC_FEATURE , "CSC feature");
        attributes.put(SENSOR_LOCATION , "Sensor location");
        attributes.put(SC_CONTROL_POINT , "SC control point");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
