package com.pmdsolutions.gentiantestapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;


import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by PMD Solutions on 8/25/2015.
 */
public class BLEService extends BluetoothGattCallback {

    private final UUID INFORMATION_SERVICE      = UUID.fromString("0000FFE0-0000-1000-8000-00805f9b34fb");
    private final UUID FIRMWARE_REVISION        = UUID.fromString("0000FFE1-0000-1000-8000-00805f9b34fb");
    private final UUID RENAME_CHAR              = UUID.fromString("0000FFE2-0000-1000-8000-00805f9b34fb");
    private final UUID APP_TIME                 = UUID.fromString("0000FFE3-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_FNAME            = UUID.fromString("0000FFE4-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_SNAME            = UUID.fromString("0000FFE5-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_SEX              = UUID.fromString("0000FFE6-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_DOB              = UUID.fromString("0000FFE7-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_DOA              = UUID.fromString("0000FFE8-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_BED              = UUID.fromString("0000FFE9-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_ROOM             = UUID.fromString("0000FFEA-0000-1000-8000-00805f9b34fb");
    private final UUID PATIENT_WARD             = UUID.fromString("0000FFEB-0000-1000-8000-00805f9b34fb");

    private final UUID DATA_SERVICE             = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
    private final UUID LATEST_DATA              = UUID.fromString("0000FFF1-0000-1000-8000-00805f9b34fb");
    private final UUID THRESHOLDS               = UUID.fromString("0000FFF2-0000-1000-8000-00805f9b34fb");
    private final UUID SILENCE_ALARM            = UUID.fromString("0000FFF3-0000-1000-8000-00805f9b34fb");
    private final UUID MAINTENANCE              = UUID.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");
    private final UUID TREND_DATA               = UUID.fromString("0000FFF5-0000-1000-8000-00805f9b34fb");
    private final UUID STREAMING                = UUID.fromString("0000FFF6-0000-1000-8000-00805f9b34fb");
    private final UUID STREAMING_CONFIG         = UUID.fromString("0000FFF7-0000-1000-8000-00805f9b34fb");
    private final UUID ERROR_CODES              = UUID.fromString("0000FFF8-0000-1000-8000-00805f9b34fb");
    private final UUID ALERT_CODE               = UUID.fromString("0000FFF9-0000-1000-8000-00805f9b34fb");
    private final UUID LOBE_STATUS              = UUID.fromString("0000FFFA-0000-1000-8000-00805f9b34fb");
    private final UUID TRANSFER_LOBE            = UUID.fromString("0000FFFB-0000-1000-8000-00805f9b34fb");
    private final UUID SECURITY_KEY             = UUID.fromString("0000FFFD-0000-1000-8000-00805f9b34fb");

    private final UUID DESCRIPTOR             = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private int i = 0;

    private int disconnectionMode;

    private int mState = 0;
    private boolean connectionState;

    private byte maintChar = 00000000;

    private void reset() {
        mState = 0;
    }

    private void advance() {
        mState++;
    }

    private static BLEService instance;
    private BluetoothGatt mConnectedGatt;
    private Context BLEContext;
    private String TAG = BLEService.class.getSimpleName();
    private ArrayList<Byte> data = new ArrayList<>();
    private BluetoothGattService dataService, informationService;
    private int mode = 5;

    public int trendCounter;
    public int modCounter = 0;
    private String name;

    public BLEService(Context context){
        instance = this;
    }
    public void connectDevice(BluetoothDevice targetDevice, int i){
        mode = i;
        mConnectedGatt = targetDevice.connectGatt(BLEContext, false, this);
        name = targetDevice.getName();
    }

    public void renameDevice(String renameDevice, String renameMRN) {
        BluetoothGattCharacteristic renameCharacteristic = null;
        Log.wtf(TAG,renameDevice + " --> " + renameMRN);
        byte[] packet = renameMRN.getBytes();
        Log.wtf(TAG, "Name: " + packet.toString());
        if(mConnectedGatt != null){
            informationService = mConnectedGatt.getService(INFORMATION_SERVICE);
        }

        if(informationService != null){
            renameCharacteristic = informationService.getCharacteristic(RENAME_CHAR);
        }
        if(renameCharacteristic != null){
            renameCharacteristic.setValue(packet);

        }
        mConnectedGatt.writeCharacteristic(renameCharacteristic);
    }


    public static BLEService getInstance() {
        return instance;
    }



    private void readData(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic;
        Log.wtf("Read Data", "Should not be called");
        switch (mState) {
            case 0:
                Log.i(TAG, "reading Status");
                characteristic = gatt.getService(DATA_SERVICE)
                        .getCharacteristic(LATEST_DATA);
                break;

            case 1:
                Log.i(TAG, "Reading Trend Data");
                if (modCounter ==0){
                    Log.i(TAG, "getting Data 1");
                    advance();
                    characteristic = gatt.getService(DATA_SERVICE)
                            .getCharacteristic(LOBE_STATUS);
                }
                else{
                    characteristic = gatt.getService(DATA_SERVICE)
                            .getCharacteristic(TREND_DATA);
                }
                break;

            case 2:
                Log.i(TAG, "getting Data 1");
                characteristic = gatt.getService(DATA_SERVICE)
                        .getCharacteristic(LOBE_STATUS);
                break;

            default:
                mConnectedGatt.close();
                Log.i(TAG, "All Data Read, disconnecting");
                return;
        }

        gatt.readCharacteristic(characteristic);
    }


    /*
     * Enable notification of changes on the data characteristic for each sensor
     * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
     * configuration descriptor.
     */
    private void setNotifyNextSensor(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic;
        switch (mState) {
            case 0:
//                    Log.d(TAG, "Set notify pressure cal");
//                    characteristic = gatt.getService(MAIN_SERVICE)
//                            .getCharacteristic(DATA_CHAR);
//                    break;
//
//                default:
//                    mHandler.sendEmptyMessage(MSG_DISMISS);
//                    Log.i(TAG, "All Sensors Enabled");
//                    return;
//            }
//
//            bytesp1 = new ArrayList<>();
//            bytesp2 = new ArrayList<>();
//            bytesv = new ArrayList<>();
//
//            //Enable local notifications
//            gatt.setCharacteristicNotification(characteristic, true);
//            //Enabled remote notifications
//            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
//            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(desc);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "Connection State Change: " + status + " -> " + connectionState(newState));
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
            Log.wtf("Status: ", "Connected");

            gatt.discoverServices();

        } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
            Log.wtf("Status: ", "Disconnected");
            mConnectedGatt.close();
            //startScan();
            //mHandler.sendEmptyMessage(MSG_DISMISS);
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
            //reset();
            Log.wtf("Status: ", "Error " + status);
            mConnectedGatt.connect();
            //mode = 5;
            //SICActivity.getInstance().reconnect(mConnectedGatt.getDevice());
            // gatt.close();
            // runOnUiThread(reScan);
        }
    }
    //
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.wtf(TAG, "Services Discovered: " + status);
        //mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */

        reset();
        writeSecurity();

    }

    private void writeSecurity() {
        BluetoothGattCharacteristic securityCharacteristic = null;

        byte[] packet = new byte [2];
        packet[0] = (byte) 0xc8;
        packet[1] = (byte) 0x47;
        if(dataService == null){
            if(mConnectedGatt != null){
                dataService = mConnectedGatt.getService(DATA_SERVICE);
            }
        }
        if(dataService != null){
            securityCharacteristic = dataService.getCharacteristic(SECURITY_KEY );
        }
        if(securityCharacteristic != null){
            securityCharacteristic.setValue(packet);

            mConnectedGatt.writeCharacteristic(securityCharacteristic);
        }
    }

    //
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
       // For each read, pass the data up to the UI thread to update the display
        Log.wtf(TAG, "Characteristic Read : " + characteristic.getUuid().toString());
        boolean isDataPacket = false;
        //checks if packets are data packets
//        for(UUID uuid : DATA_PACKETS){
//            if(characteristic.getUuid().toString().equals(uuid.toString())){
//                isDataPacket = true;
//                break;
//            }
//        }
        //if packet is error codes

        //if packet is battery level
        if(characteristic.getUuid().toString().equals(TREND_DATA.toString())){
            trendCounter--;
            for(byte b : characteristic.getValue()){
                data.add(b);
                Log.wtf(TAG, "Trend data:" + b + "");
            }
            if (trendCounter <= 0){

            }
            else {
                mState--;
            }

        }

        else if(characteristic.getUuid().toString().equals(LOBE_STATUS.toString())){
            Log.d(TAG, "Status has been read");

        }

        else if(characteristic.getUuid().toString().equals(FIRMWARE_REVISION.toString())){
            Log.d(TAG, "Errors has been read");
            final String firmwareRev = Byte.toString(characteristic.getValue()[0]) + "."+ Byte.toString(characteristic.getValue()[1]);
            final String bluetoothRev = Byte.toString(characteristic.getValue()[2]) + "."+ Byte.toString(characteristic.getValue()[3]);
            SICActivity.getInstance().setErrors(firmwareRev, bluetoothRev);
            startNotifications();
        }









//        else if(characteristic.getUuid().toString().equals(.toString())){
//            ResultActivity.getInstance().setBatteryCharge(characteristic.getValue());
//            try {
//                String decodedRecord = new String(characteristic.getValue(),"UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            Log.d("DEBUG", "decoded String : " + ByteArrayToString(characteristic.getValue()));
//            Log.d(TAG, characteristic.getValue().toString());
//        }
//        else if(characteristic.getUuid().toString().equals(TREND_DATA.toString())){
//            ResultActivity.getInstance().setBatteryCharge(characteristic.getValue());
//            try {
//                String decodedRecord = new String(characteristic.getValue(),"UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            Log.d("DEBUG", "decoded String : " + ByteArrayToString(characteristic.getValue()));
//            Log.d(TAG, characteristic.getValue().toString());
//        }
//        //if packet is a data packet

        advance();
        //readData(gatt);
    }

    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }

    public void activateLED(int led) {
        BluetoothGattCharacteristic characteristic;
        //new byte [] finisher = 0x01;

        characteristic = mConnectedGatt.getService(DATA_SERVICE)
                .getCharacteristic(MAINTENANCE);
        if (led == 1) {
            maintChar ^= 1 << 5;
        }
        if (led == 2) {
            maintChar ^= 1 << 4;
        }
        if (led == 3) {
            maintChar ^= 1 << 3;
        }
        if (led == 4) {
            maintChar ^= 1 << 2;
        }
        characteristic.setValue(new byte[]{maintChar});
        Log.wtf(TAG, "Value is: " + characteristic.getValue().toString());
        mConnectedGatt.writeCharacteristic(characteristic);

    }


        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic
        characteristic,int status){
            Log.d(TAG, "Written to " +characteristic.getUuid().toString()+": " + status);
            boolean isDataPacket = false;
//            for (UUID uuid : DATA_PACKETS) {
//                if (characteristic.getUuid().toString().equals(uuid.toString())) {
//                    isDataPacket = true;
//                    break;
//                }
//            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (isDataPacket) {

                }
                if (characteristic.getUuid().toString().equals(RENAME_CHAR.toString())) {
                }

                if(characteristic.getUuid().toString().equals(THRESHOLDS.toString())){
                    Log.d(TAG, "Status has been written");
                    if (mode == 2){
                        Log.d(TAG, "not Null");

                        mode = 0;
                    }
                    else{

                    }

                }
                if (characteristic.getUuid().toString().equals(LOBE_STATUS.toString())) {

                }
                if (characteristic.getUuid().toString().equals(ALERT_CODE.toString())) {
                    //ResultActivity.getInstance().statusWritten();
                }
                if (characteristic.getUuid().toString().equals(SECURITY_KEY.toString())) {
                    switch (mode){
                        case 1:

                            //readData(gatt);
                            break;
                        case 2:

                            break;
                        case 3:

                            break;
                        case 4:
                           startStreaming();
                            break;
                        case 5:

                            break;
                        default:
                            break;
                    }
                }

            } else {
                Log.e(TAG, "Write error: " + status);
                if (characteristic.getUuid().toString().equals(RENAME_CHAR.toString())) {

                    mConnectedGatt.disconnect();
                }
            }


        }

//
//
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            /*
//             * After notifications are enabled, all updates from the device on characteristic
//             * value changes will be posted here.  Similar to read, we hand these up to the
//             * UI thread to update the display.
//             */
//            if (DATA_CHAR.equals(characteristic.getUuid())) {
//                //Log.wtf(TAG, "still running: " + stillRunning);
//                if ((sicAct.stillRunning)) {
//                    mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
//                }
//                else{
//                    Log.wtf(TAG, "Disabling Sensors");
//                    disableSensors(gatt);                }
//            }
//
//
//        }
//
//
//
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            //Once notifications are enabled, we move to the next sensor and start over with enable
//            advance();
//            enableNextSensor(gatt);
//        }
//
//        private void disableSensors(BluetoothGatt gatt) {
//            BluetoothGattCharacteristic characteristic;
//            Log.d(TAG, "Disabling Sensors");
//            characteristic = gatt.getService(REMAME_SERVICE)
//                    .getCharacteristic(FINISH);
//            byte finisher = 0x01;
//            characteristic.setValue(new byte[] {finisher});
//
//            gatt.writeCharacteristic(characteristic);
//
//            finishStreaming();
//            sicAct.stillRunning = true;
//        }
//
//        private void finishStreaming() {
//
//        }
//
//        @Override
//        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            Log.d(TAG, "Remote RSSI: "+rssi);
//        }


    //
    private String connectionState(int status) {
        switch (status) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting";
            default:
                return String.valueOf(status);
        }
    }

    public void writeThresholds(int upperThreshold, int lowerThreshold) {
        BluetoothGattCharacteristic thresholdCharacteristic = null;
        int[] thresholds = {upperThreshold, lowerThreshold};

        byte[] packet = new byte [2];
        packet[0] = (byte) thresholds[0];
        packet[1] = (byte) thresholds[1];
        for (byte x : packet){
            Log.wtf("Packet is: ", x + "");
        }Log.wtf(TAG, "Thresholds --> "+ upperThreshold + ", " + lowerThreshold);
        if(dataService == null){
            if(mConnectedGatt != null){
                dataService = mConnectedGatt.getService(DATA_SERVICE);
            }
        }
        if(dataService != null){
            thresholdCharacteristic = dataService.getCharacteristic(THRESHOLDS);
        }
        if(thresholdCharacteristic != null){
            thresholdCharacteristic.setValue(packet);

            mConnectedGatt.writeCharacteristic(thresholdCharacteristic);
        }
    }

    public void disconnect(int i) {
        disconnectionMode = i;
        if(mConnectedGatt!=null){
            mConnectedGatt.disconnect();
            switch(disconnectionMode){
                case 1:

                    break;
            }
        }
        else{
            Log.wtf("CONNECTED GATT", "IS NULL");
            if (i==1){

            }
        }
//
//        //mConnectedGatt = null;
    }

    public void getStatus() {
        BluetoothGattCharacteristic errorCharacteristic = null;
        if(dataService == null){

            dataService = mConnectedGatt.getService(DATA_SERVICE);

        }
        if(dataService != null){
            errorCharacteristic = dataService.getCharacteristic(LOBE_STATUS);
        }
        if(errorCharacteristic != null){
            mConnectedGatt.readCharacteristic(errorCharacteristic);
        }
    }

    public void writeStatus(boolean pause, byte statusString) {
        BluetoothGattCharacteristic renameCharacteristic = null;
        Log.wtf(TAG, "Status String is: " + statusString);
        String s =("0000000" + Integer.toBinaryString(0xFF & statusString)).replaceAll(".*(.{8})$", "$1");
        Log.wtf("String is :",s);
        byte b2;
        if (pause){
            b2= 1;
        }
        else{
            b2 = 0;
        }
        byte b3 = 100;

        byte[] array = {statusString, b2, b3};
        Log.wtf(TAG, array.length + "");
        Log.wtf(TAG, array[0] + "" + ", " + array[1]+ ", " + array[2]);

        if(dataService == null){
            if(mConnectedGatt != null){
                dataService = mConnectedGatt.getService(DATA_SERVICE);
            }
        }
        if(dataService != null){
            renameCharacteristic = dataService.getCharacteristic(LOBE_STATUS);
        }
        if(renameCharacteristic != null){
            renameCharacteristic.setValue(array);
            mConnectedGatt.writeCharacteristic(renameCharacteristic);
        }
    }

    public void writeAlert() {
        BluetoothGattCharacteristic thresholdCharacteristic = null;
        String packet = "10";

        if(dataService == null){
            if(mConnectedGatt != null){
                dataService = mConnectedGatt.getService(DATA_SERVICE);
            }
        }
        if(dataService != null){
            thresholdCharacteristic = dataService.getCharacteristic(ALERT_CODE);
        }
        if(thresholdCharacteristic != null){
            thresholdCharacteristic.setValue(packet);
            mConnectedGatt.writeCharacteristic(thresholdCharacteristic);
        }
    }

    public void sendMaintChar(int i) {
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        readErrors();

    }

    private void readErrors() {
        BluetoothGattCharacteristic characteristic;
        characteristic = mConnectedGatt.getService(INFORMATION_SERVICE)
                .getCharacteristic(FIRMWARE_REVISION);

        mConnectedGatt.readCharacteristic(characteristic);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        SICActivity.getInstance().newData(characteristic);

    }

    private void startNotifications() {
        BluetoothGattCharacteristic characteristic;
        byte i = 0x01;
        Log.d(TAG, "Enabling Data Characteristic");
        characteristic = mConnectedGatt.getService(DATA_SERVICE)
                .getCharacteristic(STREAMING_CONFIG);
        characteristic.setValue(new byte[]{i});

        mConnectedGatt.requestConnectionPriority(1);
        mConnectedGatt.writeCharacteristic(characteristic);
    }

    public void startStreaming() {
        BluetoothGattCharacteristic characteristic;

        Log.d(TAG, "Set notify on Data characteristic");
        characteristic = mConnectedGatt.getService(DATA_SERVICE)
                        .getCharacteristic(STREAMING);



        //Enable local notifications
        mConnectedGatt.setCharacteristicNotification(characteristic, true);
        //Enabled remote notifications
        BluetoothGattDescriptor desc = characteristic.getDescriptor(DESCRIPTOR);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mConnectedGatt.writeDescriptor(desc);
    }

    public boolean getConnectionState() {
        connectionState = false;
        if (mConnectedGatt != null) {
            if (mConnectedGatt.getDevice() != null) {
                if (mConnectedGatt.getConnectionState(mConnectedGatt.getDevice()) == 1 || mConnectedGatt.getConnectionState(mConnectedGatt.getDevice()) == 2) {
                    connectionState = true;
                }
            }
        }
        return connectionState;
    }

}


