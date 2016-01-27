package com.pmdsolutions.gentiantestapp;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by PMD Solutions on 8/25/2015.
 */
public class BLEScanCallback extends ScanCallback {

    private String deviceName;
    private int scanCode;
    private String settingsOption;
    private ArrayList<BluetoothDevice> mDevices;
    private BLEManager mBLEManager;

    private final String TAG = "SCANNING";

    public BLEScanCallback(String dName, int code, Context context){
        this.deviceName = dName;
        this.scanCode = code;
        mDevices = new ArrayList<>();
        mDevices.clear();
        mBLEManager = new BLEManager(context);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        Log.wtf(TAG, "Scan Mode: Scanning New Device");
        for (ScanResult x: results){
            if (x.getDevice().getAddress().equalsIgnoreCase(deviceName)){
               // DashboardActivity.getInstance().showAdvertisment(x.getScanRecord().getBytes());
            }
        }

    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        BluetoothDevice device = result.getDevice();
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + result.getRssi());
        //printScanRecord(result.getScanRecord().getBytes());

        switch(scanCode){
            case 1:
                Log.i(TAG, "Scan Mode: Scanning New Patient Device");
                if (device.getName() != null) {
                    Log.wtf(TAG, device.getName() + ", " + deviceName);
                    if (device.getName().equalsIgnoreCase(deviceName)) {

                    }
                }
                break;
            case 2:
                Log.i(TAG, "Scan Mode: Scanning Existing Patient Device");
                if (device.getName() != null) {
                    if (device.getName().equalsIgnoreCase(deviceName)) {

                        mBLEManager.setDevice(device);
                        mBLEManager.connectDevice(1);
                    }
                }
                break;
            case 3:
                Log.i(TAG, "Scan Mode: Scanning Device for Maintenance");
                if (device.getName() != null) {
                    Log.i(TAG, deviceName);
                    if (device.getName().equalsIgnoreCase(deviceName)) {
                        mBLEManager.setDevice(device);
                        mBLEManager.connectDevice(3);
                    }
                }
                break;
            case 4:
                Log.i(TAG, "Scan Mode: Scanning all nearby Devices");
                if (device.getName() != null) {
                    if (mDevices.contains(device)) {
                        Log.i(TAG, "Device already scanned: " + device.getName());
                    } else {
                        mDevices.add(device);

                    }
                }
                break;
            case 5:
                Log.i(TAG, "Scan Mode: Scanning all nearby Renamed Devices");

                break;
        }



    }

    public void printScanRecord (byte[] scanRecord) {

        // Simply print all raw bytes
        try {
            String decodedRecord = new String(scanRecord,"UTF-8");
            Log.d("DEBUG","decoded String : " + ByteArrayToString(scanRecord));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Parse data bytes into individual records
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);


        // Print individual records
        if (records.size() == 0) {
            Log.i("DEBUG", "Scan Record Empty");
        } else {
            Log.i("DEBUG", "Scan Record: " + TextUtils.join(",", records));
        }

    }


    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }


    public static class AdRecord {

        public AdRecord(int length, int type, byte[] data) {
            String decodedRecord = "";
            try {
                decodedRecord = new String(data,"UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d("DEBUG", "Length: " + length + " Type : " + type + " Data : " + ByteArrayToString(data));
        }

        // ...

        public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
            List<AdRecord> records = new ArrayList<AdRecord>();

            int index = 0;
            while (index < scanRecord.length) {
                int length = scanRecord[index++];
                //Done once we run out of records
                if (length == 0) break;

                int type = scanRecord[index];
                //Done if our record isn't a valid type
                if (type == 0) break;

                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

                records.add(new AdRecord(length, type, data));
                //Advance
                index += length;
            }

            return records;
        }

        // ...
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);

    }

}
