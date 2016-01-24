package com.pmdsolutions.gentiantestapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by PMD Solutions on 30/03/2015.
 */
public class SICActivity extends Activity implements BluetoothAdapter.LeScanCallback, View.OnClickListener {


    private static final String TAG = "BluetoothGattActivity";

    private static final UUID MAIN_SERVICE = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
    private static final UUID DATA_CHAR = UUID.fromString("0000FFF6-0000-1000-8000-00805f9b34fb");

    private static final UUID REMAME_SERVICE = UUID.fromString("0000FFe0-0000-1000-8000-00805f9b34fb");
    private static final UUID REMAME_CHAR = UUID.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");

    private static final UUID SILENCE_CHAR = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb");
    private static final UUID ERROR_CHAR = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private static final UUID CONFIG_CHAR = UUID.fromString("0000FFF7-0000-1000-8000-00805f9b34fb");

    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID FINISH  = UUID.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");

    private final UUID SECURITY_KEY             = UUID.fromString("0000FFFD-0000-1000-8000-00805f9b34fb");

    private final UUID BATTERY_SERVICE_UUID	 	 = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private final UUID BATTERY_CHAR_UUID		 = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");


    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private SeekBar xBar, yBar, zBar;
    //private TextView xVal, yVal, zVal, vRefVal, p1Val, p2Val;

    private ImageButton redBtn, greenBtn, blueBtn, sounderBtn;

    private Boolean red = false, green = false, blue = false, sounder = false;

    //private CustomButtonBlue start, stop;

    private ProgressDialog mProgress;

    private static SICActivity instance;

    private MediaScannerConnection MSC;

    GraphView p1graph;
    GraphView p2graph;
    GraphView vgraph;

    private TextView usl, lsl, vref;

    BluetoothGatt gatt2;
    LineGraphSeries<DataPoint> p1Series;
    LineGraphSeries<DataPoint> p2Series;
    LineGraphSeries<DataPoint> vSeries;

    private ArrayList<Byte> bytesp1;
    private ArrayList<Byte> bytesp2;
    private ArrayList<Byte> bytesv;

    private int counter = 0;

    String deviceName;

    private boolean logging = false;

    SICActivity sicAct = SICActivity.this;

    File outfile;

    private TextView label;

    private int mState;

    public String filer;

    private boolean found = false;

    BluetoothDevice currentDevice;

    //private BlueManager blueMan;

    private TextView battLevel, tvFirmware, tvBluetooth;

    public boolean stillRunning = true;

    private static LoginHandler  mHandler = null;

    private Handler mHand = new Handler();

    private Handler battTimerHandler;

    private static ProgressDialog progressDialog;

    private Handler errTimerHandler;

    private String battCharge;
    private String firmware;
    private String bluetooth;

    private byte maintChar = 00000000;

    private Runnable getBatteryTimer = new Runnable()
    {
        public void run()
        {
            //getBattery();
            battTimerHandler.postDelayed(this, 60000);
        }
    };

    private Runnable getErrorTimer = new Runnable()
    {
        public void run()
        {
           // getErrors();
            //battTimerHandler.postDelayed(this, 60000);
        }
    };

    private Runnable reScan = new Runnable()
    {
        public void run()
        {
            startScan();
        }
    };

    private ImageView firmwareIV, bluetoothIV;



    protected void onCreate(Bundle savedInstanceState) {
        maintChar = 00000000;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_sic);
        setProgressBarIndeterminate(true);

        progressDialog = ProgressDialog.show(this, "Connecting...",
                "Please Wait", true);

        instance = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       //Log.wtf("File Name is: ", filer);
        /*
       xVal = (TextView) findViewById(R.id.xVal);
       yVal = (TextView) findViewById(R.id.yVal);
       zVal = (TextView) findViewById(R.id.zVal);
       vRefVal = (TextView) findViewById(R.id.refVal);
       p1Val = (TextView) findViewById(R.id.p1Val);
       p2Val = (TextView) findViewById(R.id.p2Val);
       */

        //local change to code
        xBar = (SeekBar) findViewById(R.id.xBar);
        yBar = (SeekBar) findViewById(R.id.yBar);
        zBar = (SeekBar) findViewById(R.id.zBar);
        usl = (TextView) findViewById(R.id.tvUSL);
        lsl = (TextView) findViewById(R.id.tvLSL);
        vref = (TextView) findViewById(R.id.tvRef);
        //redBtn = (ImageButton) findViewById(R.id.btnRed);
        //blueBtn = (ImageButton) findViewById(R.id.btnBlue);
        //greenBtn = (ImageButton) findViewById(R.id.btnGreen);
        //sounderBtn = (ImageButton) findViewById(R.id.btnSounder);
        firmwareIV = (ImageView) findViewById(R.id.firmware);
        bluetoothIV = (ImageView) findViewById(R.id.bluetooth);
        tvFirmware = (TextView) findViewById(R.id.tvFirmRev);
        tvBluetooth = (TextView) findViewById(R.id.tvBlueRev);

        battLevel =(TextView) findViewById(R.id.batteryLevelTV);
        //start = (CustomButtonBlue) findViewById(R.id.btnStart);
        attachOnClickListeners();

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mDevices = new ArrayList<>();

        mProgress = new ProgressDialog(this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);

        p1graph = new GraphView(this);
        p2graph = new GraphView(this);
        vgraph = new GraphView(this);
        p1Graph();

        Intent startIntent = getIntent();
        deviceName = startIntent.getExtras().getString("DEVICE");
        int mode = startIntent.getExtras().getInt("MODE");

        if (mode == 1){
            logging = false;
        }
        else if (mode == 2){
            logging = true;
        }

        label = (TextView) findViewById(R.id.label);
        if (logging){
            label.setText("Research and Development");
        }
        else {
            label.setText(deviceName);
        }
        errTimerHandler = new Handler();

        //blueMan = new BlueManager(getApplicationContext());
        startScan();
        stillRunning = true;
        if(mHandler == null)
            mHandler = new LoginHandler(this);
        else {
            mHandler.setTarget(this);
        }

        Calendar now = Calendar.getInstance();
        filer = 	now.get(Calendar.DAY_OF_MONTH) +
                "-" +
                (now.get(Calendar.MONTH) + 1) +
                "-" +
                now.get(Calendar.YEAR) +
                "_" +
                now.get(Calendar.HOUR_OF_DAY) +
                "-" +
                now.get(Calendar.MINUTE) +
                "-" +
                now.get(Calendar.SECOND) +
                "-" +
                now.get(Calendar.MILLISECOND);

    }


    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        clearDisplayValues();
    }

    private void attachOnClickListeners(){
        LinearLayout rl = (LinearLayout)findViewById(R.id.siclayout);
        ArrayList<View> touchables = rl.getTouchables();
        //sets each touchable item in the layout's on click to itself (see onClick())
        for(View touchable : touchables)
        {
            if(touchable instanceof Button || touchable instanceof ImageButton){
                touchable.setOnClickListener(this);
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        //Make sure dialog is hidden
        mProgress.dismiss();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.close();
            mConnectedGatt = null;
        }

        //filename = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        //getMenuInflater().inflate(R.menu.main, menu);
        //Add any device elements we've discovered to the overflow menu


        return true;
    }

    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnSICHome:

                new AlertDialog.Builder(SICActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Testing Finished")
                        .setCancelable(false)
                        .setMessage("Are you sure you wish to finish testing?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sicAct.stillRunning = false;

                                if (outfile != null){
                                    //rescanSD(outfile);
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mConnectedGatt != null){
                                                    mConnectedGatt.close();
                                                    battTimerHandler.removeCallbacks(null);
                                                }

                                                mHandler.removeCallbacks(null);
                                                finish();
                                            }
                                        }, 2000);
                                    }
                                });


                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();


                break;

            case R.id.btnSilence:
                silenceAlarm();
                break;

            case R.id.btnRed:
                activateLED(1);
                break;

            case R.id.btnGreen:
                activateLED(2);
                break;

            case R.id.btnBlue:
                activateLED(3);
                break;

            case R.id.btnSounder:
                activateLED(4);
                break;

            default:
                break;
        }
    }

    private void writeSecurity() {
        BluetoothGattCharacteristic securityCharacteristic = null;
        BluetoothGattService dataService = null;
        byte[] packet = new byte [2];
        packet[0] = (byte) 0xc8;
        packet[1] = (byte) 0x47;

            if(mConnectedGatt != null){
                dataService = mConnectedGatt.getService(MAIN_SERVICE);
            }


            securityCharacteristic = dataService.getCharacteristic(SECURITY_KEY );

        if(securityCharacteristic != null){
            securityCharacteristic.setValue(packet);

            mConnectedGatt.writeCharacteristic(securityCharacteristic);
        }
    }

    private void activateLED(int led) {
        BluetoothGattCharacteristic characteristic;
        //new byte [] finisher = 0x01;

        characteristic = gatt2.getService(MAIN_SERVICE)
                .getCharacteristic(FINISH);
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
        characteristic.setValue(new byte[] {maintChar});
        gatt2.writeCharacteristic(characteristic);

    }

    public static int convert(int n) {
        return Integer.valueOf(String.valueOf(n), 16);
    }

    private void silenceAlarm() {
        BluetoothGattCharacteristic characteristic;
        //Log.wtf("gatt is :", gatt2 + "");
        characteristic = gatt2.getService(REMAME_SERVICE)
                .getCharacteristic(SILENCE_CHAR);
        characteristic.setValue(new byte[] {0x01});

        gatt2.writeCharacteristic(characteristic);

    }

    private void getErrors() {
        BluetoothGattCharacteristic characteristic;
        //Log.wtf("gatt is :", gatt2 + "");
        characteristic = gatt2.getService(REMAME_SERVICE)
                .getCharacteristic(ERROR_CHAR);
        //characteristic.setValue(new byte[] {0x02});

        gatt2.readCharacteristic(characteristic);

    }

    private void stopStreaming() {

    }

    private void startStreaming() {
        if (mDevices.size() == 0) {
            mDevices.clear();
            mBluetoothAdapter.startLeScan(this);
            setProgressBarIndeterminateVisibility(true);
            mHandler.postDelayed(mStopRunnable, 2500);
            //start.setText("Start");
        }
    }
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                mDevices.clear();
                startScan();
                return true;
            default:
                //Obtain the discovered device to connect with
                BluetoothDevice device = mDevices.get(item.getItemId());
                Log.i(TAG, "Connecting to " + device.getName());
                /*
                 * Make a connection with the device using the special LE-specific
                 * connectGatt() method, passing in a callback for GATT events

                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
                return super.onOptionsItemSelected(item);
        }
    }
    */

    private static void clearDisplayValues() {

    }
    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        if (mConnectedGatt != null){
            mConnectedGatt.close();
        }

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        //blueMan = new BlueManager(getApplicationContext());
        mDevices.clear();
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);

        mHand.postDelayed(mStopRunnable, 5000);

    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
        found = false;
        for (BluetoothDevice x : mDevices){
            if (x.getName() == null){
               // Log.wtf(TAG, "Null Name");
            }

            else if (x.getName().equalsIgnoreCase(deviceName)){
               // Log.wtf(TAG, "Device name is: " + deviceName);
                if (progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                BluetoothDevice device = x;
                found = true;
                currentDevice = device;
                mConnectedGatt = device.connectGatt(this, false, mGattCallback);
                //Display progress UI
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
                battTimerHandler = new Handler();
                battTimerHandler.postDelayed(getBatteryTimer,10000);
            }

        }

        if (!found && stillRunning){
//            new AlertDialog.Builder(SICActivity.this)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("Device Not Found")
//                    .setCancelable(false)
//                    .setMessage("No Device with this name could be found. Please ensure the Barcode you are using is correct and try again.")
//                    .setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                        }
//                    })
//                    .show();
           // Log.wtf("Device ", "not found");
            startScan();
        }
    }

    /* BluetoothAdapter.LeScanCallback */

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */

        mDevices.add(device);
        //Update the overflow menu
        //invalidateOptionsMenu();

    }


    /*
         * In this callback, we've created a bit of a state machine to enforce that only
         * one characteristic be read or written at a time until all of our sensors
         * are enabled and we are registered to get notifications.
         */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /* State Machine Tracking */
        private int mState = 0;

        private void reset() { mState = 0; }

        private void advance() { mState++; }

        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            gatt2 = gatt;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Renaming");
                    characteristic = gatt.getService(REMAME_SERVICE)
                            .getCharacteristic(ERROR_CHAR);

                    gatt.readCharacteristic(characteristic);
                    break;

               case 1:
                Log.d(TAG, "Enabling pressure cal");
                characteristic = gatt.getService(MAIN_SERVICE)
                        .getCharacteristic(CONFIG_CHAR);
                characteristic.setValue(new byte[] {0x01});
                   gatt.writeCharacteristic(characteristic);
                break;


                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }


        }

        private byte[] preparePacket() {



            int packetLength = deviceName.length();
            byte[] pack = new byte[packetLength];
            //Thresholds
            //0 = upper
            //1 = lower
            //Pulled from MainActivity because BLEService does not extend Activity and therefore
            //has no access to shared preferences.
            int[] thresholds = {61, 5};

            pack[0] = (byte) packetLength;
            for(int i = 1; i <= deviceName.length(); i++){
                pack[i] = (byte) deviceName.charAt(i - 1);
            }

            int j = deviceName.length()+1;
            pack[j] = (byte) thresholds[0];
            pack[j+1] = (byte) thresholds[1];

            for(int y = (deviceName.length() + 3); y<pack.length; y++){
                pack[y] = (byte)0;
            }
            return pack;
        }



        /*
         * Read the data characteristic's value for each sensor explicitly
         */
//        private void readNextSensor(BluetoothGatt gatt) {
//            BluetoothGattCharacteristic characteristic;
//            switch (mState) {
//                case 0:
//                    Log.d(TAG, "Reading pressure cal");
//                    characteristic = gatt.getService(PRESSURE_SERVICE)
//                            .getCharacteristic(PRESSURE_CAL_CHAR);
//                    break;
//                case 1:
//                    Log.d(TAG, "Reading pressure");
//                    characteristic = gatt.getService(PRESSURE_SERVICE)
//                            .getCharacteristic(PRESSURE_DATA_CHAR);
//                    break;
//                case 2:
//                    Log.d(TAG, "Reading humidity");
//                    characteristic = gatt.getService(HUMIDITY_SERVICE)
//                            .getCharacteristic(HUMIDITY_DATA_CHAR);
//                    break;
//                default:
//                    mHandler.sendEmptyMessage(MSG_DISMISS);
//                    Log.i(TAG, "All Sensors Enabled");
//                    return;
//            }
//
//            gatt.readCharacteristic(characteristic);
//        }

        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 1:
                    Log.d(TAG, "Set notify pressure cal");
                    characteristic = gatt.getService(MAIN_SERVICE)
                            .getCharacteristic(DATA_CHAR);
                    break;

                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            bytesp1 = new ArrayList<>();
            bytesp2 = new ArrayList<>();
            bytesv = new ArrayList<>();

            //Enable local notifications
            gatt.setCharacteristicNotification(characteristic, true);
            //Enabled remote notifications
            BluetoothGattDescriptor desc = characteristic.getDescriptor(CONFIG_DESCRIPTOR);
            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(desc);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Connection State Change: " + status + " -> " + connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                errTimerHandler.postDelayed(getErrorTimer, 8000);
                gatt.discoverServices();
                mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                Log.wtf("Status: ", "Disconnected");
                //startScan();
                //mHandler.sendEmptyMessage(MSG_DISMISS);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */

               // gatt.close();
               // runOnUiThread(reScan);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: "+status);
            mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            writeSecurity();

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));

                setNotifyNextSensor(gatt);
            }
            else if (BATTERY_CHAR_UUID.equals(characteristic.getUuid())){

                final String batteryCharge = Byte.toString( characteristic.getValue()[0]);
                battCharge = batteryCharge;
                runOnUiThread(new Runnable() {
                    public void run() {
                        battLevel.setText(batteryCharge+ "%");
                    }
                });
            }

            else if (ERROR_CHAR.equals(characteristic.getUuid())){
                Log.wtf("Something", ""+characteristic.getValue()[0]);

                final String firmwareRev = Byte.toString(characteristic.getValue()[0]) + "."+ Byte.toString(characteristic.getValue()[1]);
                final String bluetoothRev = Byte.toString(characteristic.getValue()[2]) + "."+ Byte.toString(characteristic.getValue()[3]);
                firmware = firmwareRev;
                bluetooth = bluetoothRev;
               // Log.wtf(TAG, "Firmware is : " + firmware);
               // Log.wtf(TAG, "Bluetooth is : " + bluetooth);
                SICActivity.getInstance().setTextviews();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (firmware.equalsIgnoreCase(GlobalValues.Version.FIRMWARE)) {
                            firmwareIV.setImageResource(R.drawable.fgood);
                        } else {
                            firmwareIV.setImageResource(R.drawable.fbad);
                        }

                        if (bluetooth.equalsIgnoreCase(GlobalValues.Version.BLUETOOTH)) {
                            bluetoothIV.setImageResource(R.drawable.bgood);
                        } else {
                            bluetoothIV.setImageResource(R.drawable.bbad);
                        }
                    }
                });
                advance();
                enableNextSensor(gatt);
            }
            //After reading the initial value, next we enable notifications

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            Log.wtf("Streaming", characteristic.getUuid().toString() + "Status:" + status);
            if(characteristic.getUuid().toString().equalsIgnoreCase(String.valueOf(SECURITY_KEY))){
                enableNextSensor(gatt);
            }
            else{
                setNotifyNextSensor(gatt);
            }

        }



        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            /*
             * After notifications are enabled, all updates from the device on characteristic
             * value changes will be posted here.  Similar to read, we hand these up to the
             * UI thread to update the display.
             */
            if (DATA_CHAR.equals(characteristic.getUuid())) {
                //Log.wtf(TAG, "still running: " + stillRunning);
                if ((sicAct.stillRunning)) {
                    mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
                }
                else{
                    Log.wtf(TAG, "Disabling Sensors");
                    disableSensors(gatt);                }
            }


        }



        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            advance();
            enableNextSensor(gatt);
        }

        private void disableSensors(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            Log.d(TAG, "Disabling Sensors");
            characteristic = gatt.getService(MAIN_SERVICE)
                    .getCharacteristic(FINISH);
            byte finisher = 0x01;
            characteristic.setValue(new byte[] {finisher});

            gatt.writeCharacteristic(characteristic);
            
            finishStreaming();
            sicAct.stillRunning = true;
        }

        private void finishStreaming() {

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "Remote RSSI: "+rssi);
        }

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
    };



    private static class LoginHandler extends Handler {

        private WeakReference<SICActivity>    mTarget;

        LoginHandler(SICActivity target) {
            mTarget = new WeakReference<SICActivity>(target);
        }

        public void setTarget(SICActivity target) {
            mTarget = new WeakReference<SICActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            // process incoming messages here
            SICActivity activity = mTarget.get();

            BluetoothGattCharacteristic characteristic;

            switch (msg.what) {


                    case MSG_HUMIDITY:
                        characteristic = (BluetoothGattCharacteristic) msg.obj;
                        if (characteristic.getValue() == null) {
                            Log.w(TAG, "Error obtaining humidity value");
                            return;
                        }
                        updateHumidityValues(characteristic);
                        activity.updatePressureValue(characteristic);
                        break;
                    case MSG_PRESSURE:
                        characteristic = (BluetoothGattCharacteristic) msg.obj;
                        if (characteristic.getValue() == null) {
                            Log.w(TAG, "Error obtaining pressure value");
                            return;
                        }
                        activity.updatePressureValue(characteristic);
                        break;
                    case MSG_PRESSURE_CAL:
                        characteristic = (BluetoothGattCharacteristic) msg.obj;
                        if (characteristic.getValue() == null) {
                            Log.w(TAG, "Error obtaining cal value");
                            return;
                        }
                        activity.updatePressureCals(characteristic);
                        break;
                    case MSG_PROGRESS:
                        activity.mProgress.setMessage((String) msg.obj);
                        if (!activity.mProgress.isShowing()) {
                            activity.mProgress.show();
                        }
                        break;
                    case MSG_DISMISS:
                        activity.mProgress.hide();
                        View decorView = activity.getWindow().getDecorView();
                        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                        decorView.setSystemUiVisibility(uiOptions);

                        break;
                    case MSG_CLEAR:
                        clearDisplayValues();
                        break;
                }


        }
    }

    /*
     * We have a Handler to process event results on the main thread
     */
    private static final int MSG_HUMIDITY = 101;
    private static final int MSG_PRESSURE = 102;
    private static final int MSG_PRESSURE_CAL = 103;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;


    /* Methods to extract sensor data and update the UI */

    private static void updateHumidityValues(BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        int humidity = value[0];

    }

    private int[] mPressureCals;
    private void updatePressureCals(BluetoothGattCharacteristic characteristic) {

    }

    private void updatePressureValue(BluetoothGattCharacteristic characteristic) {
        counter++;
        byte[] value = characteristic.getValue();

        int x = twoBytesToShort(value[0], value[1]);
        x = x >> 6;
        int y = twoBytesToShort(value[2], value[3]);
        y = y >> 6;
        int z = twoBytesToShort(value[4], value[5]);
        z = z >> 6;
        int ref = value[6];
        ref +=2048;
        int p1 = twoBytesToShort(value[7], value[8]);
        int p2 = twoBytesToShort(value[9], value[10]);
        int spi = (int) value[11];


        Integer[] values = {x, y, z, ref, p1, p2, spi};

        xBar.setProgress(x + 512);
        yBar.setProgress(y + 512);
        zBar.setProgress(z + 512);
        /*
        xVal.setText(""+x);
        yVal.setText(""+y);
        zVal.setText(""+z);
        vRefVal.setText(""+ref);
        p1Val.setText(""+p1);
        p2Val.setText(""+p2);
        */
       // Log.wtf(TAG, "updated");
        //Log.wtf("X Value", "" + x);
        //Log.wtf("File Name before Log: ", filer);
        //DownloadWebPageTask task = new DownloadWebPageTask();
        //task.execute(values);
        p1Series.appendData(new DataPoint(counter, p1), true, 100);
        //Log.wtf("P1 VALUE: ", p1 + "");
        float mP1 = p1;
        float p1Value = (((float).000733)*mP1);
        usl.setText((String.format("%.2f", round(p1Value, 2)) + " V"));

        p2Series.appendData(new DataPoint(counter, p2), true, 100);
       // Log.wtf("P1 VALUE: ", p2 + "");
        float mP2 = p2;
        float p2Value = ((float).000733)*mP2;
        lsl.setText((String.format("%.2f", round(p2Value, 2)) + " V"));

        vSeries.appendData(new DataPoint(counter, ref), true, 100);
        float mRef = ref;
        float p3Value = ((float).000733)*mRef;
        //Log.wtf(TAG, p3Value + "");

        vref.setText((String.format("%.2f", round(p3Value, 2)) + " V"));
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

//    private void logData(int x, int y, int z, int ref, int p1, int p2){
//        //outfile = null;
//        Log.wtf("File Name is: ", filer);
//        Log.wtf(TAG, "writing data to log");
//        String directoryPath = GlobalValues.FilePaths.MAINT_DIRECTORY_PATH;
//        Calendar now = Calendar.getInstance();
//        String timestamp = 	now.get(Calendar.DAY_OF_MONTH) +
//                "-" +
//                (now.get(Calendar.MONTH) + 1) +
//                "-" +
//                now.get(Calendar.YEAR) +
//                "_" +
//                now.get(Calendar.HOUR_OF_DAY) +
//                "-" +
//                now.get(Calendar.MINUTE) +
//                "-" +
//                now.get(Calendar.SECOND) +
//                "-" +
//                now.get(Calendar.MILLISECOND);
//        final String fileName = filer + ".txt";
//        //device has already been renamed as such we need to get devices MAC address to get its original name
//        //String name = btDevice.getName();
//
//        //"2" prefixed for all error logs, "1" prefixed for maintenance logs
//        String contents = timestamp + ", " + x + ", " + y + ", " + z + ", " + ref + ", " + p1 + ", " + p2 + ", " + battCharge;
//
//        try{
//            File directory = new File(directoryPath);
//            if(directory.exists() == false){
//                directory.mkdirs();
//            }
//            outfile = new File(directoryPath, fileName);
//
//            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile, true)));
//            if (outfile.exists() == false){
//                outfile.createNewFile();
//            }
//            out.print(contents);
//            out.append("\r\n");
//            out.close();
//        }catch(Exception e){
//            Log.e(TAG, "@CREATE FILE");
//        }
//        //if(outfile != null){
//        //	rescanSD(outfile);
//        //}
//    }
//    /**
//     * Method that access the internal storage on the tablet in order to access log files
//     */
//    private void rescanSD(final File toScan){
//
//        MediaScannerConnection.MediaScannerConnectionClient MSCC = new MediaScannerConnection.MediaScannerConnectionClient(){
//
//            public void onScanCompleted(String path, Uri uri) {
//                Log.i(TAG, "Media Scan Completed.");
//            }
//            public void onMediaScannerConnected() {
//                MSC.scanFile(toScan.getAbsolutePath(), null);
//                Log.i(TAG, "Media Scanner Connected.");
//            }
//        };
//        MSC = new MediaScannerConnection(getApplicationContext(), MSCC);
//        MSC.connect();
//    }

    public static short twoBytesToShort(byte b1, byte b2) {
        return (short) ((b1 << 8) | (b2 & 0xFF));
    }

    private void p1Graph(){

        p1Series = new LineGraphSeries();
        p1Series.setColor(Color.RED);

        //p1graph.setManualYAxisBounds(127, -127);
        p1graph.addSeries(p1Series); // data
        p1graph.getViewport().setYAxisBoundsManual(true);
        p1graph.getViewport().setMaxY(4096);
        p1graph.getViewport().setMinY(-1);
        p1graph.getViewport().setScalable(false);
        p1graph.getViewport().setScrollable(false);

        //p1graph.setTitle("Upper Sensor Leg");
        p1graph.setTitleColor(R.color.PMDBlue);
        p1graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        p1graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.p1Layout);
        layout.addView(p1graph);
//////////////////////////////////////
        p2Series = new LineGraphSeries();
        p2Series.setColor(Color.BLUE);

        //p1graph.setManualYAxisBounds(127, -127);
        p2graph.addSeries(p2Series); // data
        p2graph.getViewport().setYAxisBoundsManual(true);
        p2graph.getViewport().setMaxY(4096);
        p2graph.getViewport().setMinY(-1);
        p2graph.getViewport().setScalable(false);
        p2graph.getViewport().setScrollable(false);

        //p2graph.setTitle("Lower Sensor Leg");
        p2graph.setTitleColor(R.color.PMDBlue);
        p2graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        p2graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.p2Layout);
        layout2.addView(p2graph);

        //////////////////////////////////////
        vSeries = new LineGraphSeries();
        vSeries.setColor(Color.rgb(0, 156,2));

        //p1graph.setManualYAxisBounds(127, -127);
        vgraph.addSeries(vSeries); // data
        vgraph.getViewport().setYAxisBoundsManual(true);
        vgraph.getViewport().setMaxY(4096);
        vgraph.getViewport().setMinY(-1);
        vgraph.getViewport().setScalable(false);
        vgraph.getViewport().setScrollable(false);

        //vgraph.setTitle("Reference Voltage");
        vgraph.setTitleColor(R.color.PMDBlue);
        vgraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        vgraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        RelativeLayout layout3 = (RelativeLayout) findViewById(R.id.vRefLayout);
        layout3.addView(vgraph);


    }

    public static SICActivity getInstance(){
        return instance;
    }
//    private void getBattery(){
//        BluetoothGattCharacteristic characteristic;
//
//        characteristic = gatt2.getService(BATTERY_SERVICE_UUID)
//                .getCharacteristic(BATTERY_CHAR_UUID);
//
//        gatt2.readCharacteristic(characteristic);

    //}

    public void setTextviews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tvFirmware.setText("Firmware Version: " + firmware);
                tvBluetooth.setText("Bluetooth Version: " +bluetooth);

            }
        });

    }

    ///////////////STUFF\\\\\\\\\\\\\\\\

    private class DownloadWebPageTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int x,y,z,ref,p1,p2, spi;
            x = params[0];
            y = params[1];
            z = params[2];
            ref = params[3];
            p1 = params[4];
            p2 = params[5];
            spi = params[6];
        /*
        xVal.setText(""+x);
        yVal.setText(""+y);
        zVal.setText(""+z);
        vRefVal.setText(""+ref);
        p1Val.setText(""+p1);
        p2Val.setText(""+p2);
        */
           // Log.wtf("File Name is: ", filer);
          //  Log.wtf(TAG, "writing data to log");
            String directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pmd-respirasense-logs/";
            Calendar now = Calendar.getInstance();
            String timestamp = 	now.get(Calendar.DAY_OF_MONTH) +
                    "-" +
                    (now.get(Calendar.MONTH) + 1) +
                    "-" +
                    now.get(Calendar.YEAR) +
                    "_" +
                    now.get(Calendar.HOUR_OF_DAY) +
                    "-" +
                    now.get(Calendar.MINUTE) +
                    "-" +
                    now.get(Calendar.SECOND) +
                    "-" +
                    now.get(Calendar.MILLISECOND);
            final String fileName = filer + ".txt";
            //device has already been renamed as such we need to get devices MAC address to get its original name
            //String name = btDevice.getName();

            //"2" prefixed for all error logs, "1" prefixed for maintenance logs
            String contents = timestamp + ", " + x + ", " + y + ", " + z + ", " + ref + ", " + p1 + ", " + p2 +", "+ spi + ", " + battCharge;

            try{
                File directory = new File(directoryPath);
                if(directory.exists() == false){
                    directory.mkdirs();
                }
                outfile = new File(directoryPath, fileName);

                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile, true)));
                if (outfile.exists() == false){
                    outfile.createNewFile();
                }
                out.print(contents);
                out.append("\r\n");
                out.close();
            }catch(Exception e){
                Log.e(TAG, "@CREATE FILE");
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
           // Log.wtf("AsyncTask: ", "Data Logged");
        }
    }
}

