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
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by PMD Solutions on 30/03/2015.
 */
public class SICActivity extends Activity implements BluetoothAdapter.LeScanCallback, View.OnClickListener {

    private static final String TAG = "BluetoothGattActivity";

    private static final UUID MAIN_SERVICE      = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
    private static final UUID DATA_CHAR         = UUID.fromString("0000FFF6-0000-1000-8000-00805f9b34fb");
    private static final UUID CONFIG_CHAR       = UUID.fromString("0000FFF7-0000-1000-8000-00805f9b34fb");
    private static final UUID ERROR_CHAR        = UUID.fromString("0000FFF8-0000-1000-8000-00805f9b34fb");
    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID FINISH            = UUID.fromString("0000FFF8-0000-1000-8000-00805f9b34fb");
    private static final UUID SECURITY_KEY      = UUID.fromString("0000FFFD-0000-1000-8000-00805f9b34fb");

    private TextView tvFirmware, tvBluetooth;

    private BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private ImageView firmwareIV, bluetoothIV;

    private SeekBar xBar, yBar, zBar;

    private ProgressDialog mProgress;

    private MediaScannerConnection MSC;

    private static SICActivity instance;

    private byte maintChar = 00000000;

    private String firmware, bluetooth;

    GraphView p1graph;
    GraphView p2graph;
    GraphView vgraph;

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

    private TextView label, dName;

    private int mState;

    public String filer;

    private boolean found = false;

    BluetoothDevice currentDevice;

    private int percent;

    private TextView battLevel;

    public boolean stillRunning = true;

    private static LoginHandler mHandler = null;

    private Handler mHand = new Handler();

    private String battCharge;

    private WifiManager wifiManager;

    private Handler testHandler;

    private int percentage = 0;

    private int max;

    private LoadingDialog cdd;

    private boolean isScanRunning = false;

    DBAdapter myDb;

    private BLEManager leManager;
    private int progress;

    public boolean SICRunning, fpc;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_sic);
        setProgressBarIndeterminate(true);

        SICRunning = true;

        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        instance = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        xBar = (SeekBar) findViewById(R.id.xBar);
        yBar = (SeekBar) findViewById(R.id.yBar);
        zBar = (SeekBar) findViewById(R.id.zBar);
        dName = (TextView) findViewById(R.id.label);
        firmwareIV = (ImageView) findViewById(R.id.firmware);
        bluetoothIV = (ImageView) findViewById(R.id.bluetooth);
        tvFirmware = (TextView) findViewById(R.id.tvFirmRev);
        tvBluetooth = (TextView) findViewById(R.id.tvBlueRev);


        battLevel = (TextView) findViewById(R.id.batteryLevelTV);
        attachOnClickListeners();


        leManager = new BLEManager(this);
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
        dName.setText(deviceName);
        int mode = startIntent.getExtras().getInt("MODE");

        if (mode == 1) {
            logging = false;
        } else if (mode == 2) {
            logging = true;
        }

        label = (TextView) findViewById(R.id.label);
        if (logging) {
            label.setText("Research and Development");
        } else {
            label.setText(deviceName);
        }
        stillRunning = true;
        if (mHandler == null)
            mHandler = new LoginHandler(this);
        else {
            mHandler.setTarget(this);
        }

        Calendar now = Calendar.getInstance();
        filer = now.get(Calendar.DAY_OF_MONTH) +
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
        mProgress = ProgressDialog.show(this, "Scanning...",
                "Please Wait", true);
        //fillDB();
        if (!isScanRunning){
            runOnUiThread(mStartRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
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
    }

    private void attachOnClickListeners() {
        LinearLayout rl = (LinearLayout) findViewById(R.id.siclayout);
        ArrayList<View> touchables = rl.getTouchables();
        //sets each touchable item in the layout's on click to itself (see onClick())
        for (View touchable : touchables) {
            if (touchable instanceof Button || touchable instanceof ImageButton) {
                touchable.setOnClickListener(this);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mProgress.dismiss();
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.stopLeScan(this);
            mBluetoothAdapter.disable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Disconnect from any active tag connection
        if (mConnectedGatt != null) {
            mConnectedGatt.close();
            mConnectedGatt = null;
        }
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnSICHome:

                new AlertDialog.Builder(SICActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Testing Finished")
                        .setCancelable(false)
                        .setMessage("Are you sure you wish to finish testing?")
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sicAct.stillRunning = false;
                                if (outfile != null) {
                                    rescanSD(outfile);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (mConnectedGatt != null) {
                                                    mConnectedGatt.close();

                                                }

                                                mHandler.removeCallbacks(null);
                                                if (logging) {
                                                    logData();
                                                }
                                                else{
                                                    finish();
                                                }

                                            }
                                        }, 2000);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();


                break;

            case R.id.btnRed:
                leManager.getInstance().activateLED(1);
                break;

            case R.id.btnGreen:
                leManager.getInstance().activateLED(2);
                break;

            case R.id.btnBlue:
                leManager.getInstance().activateLED(3);
                break;

            case R.id.btnSounder:
                leManager.getInstance().activateLED(4);
                break;

            case R.id.btnSilence:
                silenceAlarm();
                break;

            default:
                break;
        }
    }

    private void silenceAlarm() {
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            isScanRunning = false;
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            isScanRunning = true;
            //mBluetoothAdapter.disable();
            startScan();
        }
    };

    private void startScan() {
        Log.wtf(TAG, "Starting Scan");
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        if (mConnectedGatt != null) {
            mConnectedGatt.close();
        }
        mDevices.clear();
        mBluetoothAdapter.startLeScan(this);
        setProgressBarIndeterminateVisibility(true);
        mHand.postDelayed(mStopRunnable, 5000);
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
        characteristic.setValue(new byte[]{maintChar});
        Log.wtf(TAG, "Value is: " + characteristic.getValue().toString());
        gatt2.writeCharacteristic(characteristic);

    }

    private void stopScan() {
        Log.wtf(TAG, "Stopping scan : " + found);
        found = false;
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);

        for (BluetoothDevice x : mDevices) {
            if (x.getName() == null) {
            } else if (x.getName().equalsIgnoreCase(deviceName)&& !found) {
                found = true;
                Log.wtf(TAG, "Device name is: " + deviceName);
                Log.wtf(TAG, "Device name is: " + "CONNECTING!!!!!");
                if (!leManager.isConnected()){
                    leManager.setDevice(x);
                    Log.wtf(TAG, "Device name is: " + "CONNECT!!!!!");
                    leManager.connectDevice(4);
                }
//                BluetoothDevice device = x;
//                currentDevice = device;
//                mConnectedGatt = device.connectGatt(this, true, mGattCallback);
//                mConnectedGatt.requestConnectionPriority(1);
                //Display progress UI
                // mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
            }
        }
        if (!found && stillRunning && !leManager.isConnected()) {
            if (!isScanRunning){
                Log.wtf(TAG, "Restarting scan");
                runOnUiThread(mStartRunnable);
            }
        }
    }

    public void reconnect(BluetoothDevice d){
        leManager.setDevice(d);
        Log.wtf(TAG, "Device name is: " + "CONNECT!!!!!");
        leManager.connectDevice(4);
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

    }

    public void setTextviews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tvFirmware.setText("Firmware Version: " + firmware);
                tvBluetooth.setText("Bluetooth Version: " +bluetooth);

            }
        });

    }


    /*
         * In this callback, we've created a bit of a state machine to enforce that only
         * one characteristic be read or written at a time until all of our sensors
         * are enabled and we are registered to get notifications.
         */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        /* State Machine Tracking */
        private int mState = 0;

        private void reset() {
            mState = 0;
        }

        private void advance() {
            mState++;
        }

        /*
         * Send an enable command to each sensor by writing a configuration
         * characteristic.  This is specific to the SensorTag to keep power
         * low by disabling sensors you aren't using.
         */
        private void enableNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            gatt2 = gatt;
            Log.wtf("mState is", mState + "");
            switch (mState) {
                case 0:
                    Log.wtf(TAG, "Renaming");
                    characteristic = gatt.getService(MAIN_SERVICE)
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

        /*
         * Enable notification of changes on the data characteristic for each sensor
         * by writing the ENABLE_NOTIFICATION_VALUE flag to that characteristic's
         * configuration descriptor.
         */
        private void setNotifyNextSensor(BluetoothGatt gatt) {
            BluetoothGattCharacteristic characteristic;
            switch (mState) {
                case 0:
                    Log.d(TAG, "Set notify on Data characteristic");
                    characteristic = gatt.getService(MAIN_SERVICE)
                            .getCharacteristic(DATA_CHAR);
                    break;

                default:
                    // mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    reset();
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
            Log.wtf(TAG, "Connection State Change: " + status + " -> " + connectionState(newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */
                gatt.discoverServices();
                // mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Discovering Services..."));
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED && status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                Log.wtf("Status: ", "Disconnected");
                Integer[] values = new Integer[]{-1, -1, -1, -1, -1, -1, -1, -1};
                DownloadWebPageTask task = new DownloadWebPageTask();
                task.execute(values);
                // mConnectedGatt.close();
                //gatt.close();
                if (!isScanRunning){
                    runOnUiThread(mStartRunnable);
                }


            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: " + status);
            // mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            writeSecurity(gatt);
            //setNotifyNextSensor(gatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.wtf("Read", characteristic.getUuid().toString());
            if (ERROR_CHAR.equals(characteristic.getUuid())){
                Log.wtf("Something", "" + characteristic.getValue()[0]);



                advance();
                enableNextSensor(gatt);
            }
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial
            Log.wtf(TAG, "Written to " + characteristic.getUuid().toString() + ":" + status);
            if (characteristic.getUuid().toString().equals(SECURITY_KEY.toString())) {
                Log.wtf(TAG, "Written to Security");
//                    setNotifyNextSensor(gatt);
                setNotifyNextSensor(gatt);

            }

            if (characteristic.getUuid().toString().equals(CONFIG_CHAR.toString())) {
                Log.wtf(TAG, "Written to Config");
                advance();
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
            Log.wtf(TAG, "notification");
            if (DATA_CHAR.equals(characteristic.getUuid())) {
                // Log.wtf(TAG, "still running: " + stillRunning);
                if ((sicAct.stillRunning)) {
                    //mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
                } else {
                    // disableSensors(gatt);
                }
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Once notifications are enabled, we move to the next sensor and start over with enable
            Log.wtf(TAG, "Descriptor Written");
            enableNextSensor(gatt);

        }

        private void writeSecurity(BluetoothGatt gatt) {
            Log.wtf(TAG, "Security Written");
            BluetoothGattCharacteristic securityCharacteristic = null;
            byte[] packet = new byte [1];
            int i = 153;

            packet[0] = (byte)i;
            securityCharacteristic = gatt.getService(MAIN_SERVICE)
                    .getCharacteristic(SECURITY_KEY);
            securityCharacteristic.setValue(packet);
            Log.wtf(TAG, packet[0] + "");
            gatt.writeCharacteristic(securityCharacteristic);
        }

        private void disableSensors(BluetoothGatt gatt) {
            SICRunning = false;
            BluetoothGattCharacteristic characteristic;
            Log.d(TAG, "Disabling Sensors");
            characteristic = gatt.getService(MAIN_SERVICE)
                    .getCharacteristic(CONFIG_CHAR);
            byte finisher = 0x00;
            characteristic.setValue(new byte[]{finisher});

            gatt.writeCharacteristic(characteristic);
            sicAct.stillRunning = true;
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

    public static SICActivity getInstance(){
        return instance;
    }

    public void end() {
        finish();
    }

    public void newData(BluetoothGattCharacteristic characteristic) {
        if (DATA_CHAR.equals(characteristic.getUuid())) {
            // Log.wtf(TAG, "still running: " + stillRunning);
            if ((sicAct.stillRunning)) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
            } else {

            }
        }
    }

    public void setErrors(String firmwareRev, String bluetoothRev) {
        mProgress.dismiss();
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
    }

    private class DownloadWebPageTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {

            int x, y, z, ref, p1, p2, spi, batt;
            x = params[0];
            y = params[1];
            z = params[2];
            ref = params[3];
            p1 = params[4];
            p2 = params[5];
            spi = params[6];
            batt = params[7];



            String directoryPath = GlobalValues.FilePaths.MAINT_DIRECTORY_PATH;
            Calendar now = Calendar.getInstance();
            String timestamp = now.get(Calendar.DAY_OF_MONTH) +
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
            myDb.insertRow(timestamp, x, y, z, ref, p1, p2, spi, batt);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.wtf("AsyncTask: ", "Data Logged");
        }
    }

    private class DBtoCSV extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            //outfile = null;
            // Log.wtf("File Name is: ", filer);
            //Log.wtf(TAG, "writing data to log");
            String directoryPath = GlobalValues.FilePaths.MAINT_DIRECTORY_PATH;
            Calendar now = Calendar.getInstance();
            String timestamp = now.get(Calendar.DAY_OF_MONTH) +
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


            try {
                File directory = new File(directoryPath);
                if (directory.exists() == false) {
                    directory.mkdirs();
                }
                outfile = new File(directoryPath, fileName);

                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfile, true)));
                if (outfile.exists() == false) {
                    outfile.createNewFile();
                }
                Cursor cursor = myDb.getAllRows();
                if (cursor != null) {

                    while (cursor.moveToNext()) {
                        String contents;
                        contents = cursor.getInt(0) + ",";
                        contents += cursor.getString(1)
                                + ",";
                        contents += cursor.getInt(2)
                                + ",";
                        contents += cursor.getInt(3)
                                + ",";
                        contents += cursor.getInt(4)
                                + ",";
                        contents += cursor.getInt(5)
                                + ",";
                        contents += cursor.getInt(6)
                                + ",";
                        contents += cursor.getInt(7)
                                + ",";
                        contents += cursor.getInt(8)
                                + ",";
                        contents += cursor.getInt(9);
                        out.print(contents);
                        out.append("\r\n");
                        progress = cursor.getInt(0);
                        percent= (int)((progress* 100.0f)  / max);
                        cdd.setProgress(progress);

                        if ((percent > percentage) && !(percent>100)){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    cdd.setPrecentage(percent);
                                    //percentage = percent;
                                    percentage = percent;
                                }
                            });

                        }


                    }
                    cursor.close();
                }
                out.close();
            } catch (Exception e) {
                Log.e(TAG, "@CREATE FILE");
            }
            if (outfile != null) {
                rescanSD(outfile);
            }
            myDb.close();
            SICActivity.this.deleteDatabase("LOGS");

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            Log.wtf("AsyncTask: ", "File made");
            cdd.finish(filer);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }


    private static class LoginHandler extends Handler {

        private WeakReference<SICActivity> mTarget;

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
        String s =("0000000" + Integer.toBinaryString(0xFF & value[12])).replaceAll(".*(.{8})$", "$1");
        if (s.charAt(0)=='1'){
            fpc = true;
        }
        char[] chars = s.toCharArray();
        chars[0] = '0';
        s = String.valueOf(chars);
        byte b = Byte.parseByte(s, 2);

        int batt = b;
        battCharge = "" + batt;
        runOnUiThread(new Runnable() {
            public void run() {
                battLevel.setText(battCharge + "%");
            }
        });

        Integer[] values = {x, y, z, ref, p1, p2, spi, batt};
        xBar.setProgress(x + 512);
        yBar.setProgress(y + 512);
        zBar.setProgress(z + 512);
        if (logging) {
            DownloadWebPageTask task = new DownloadWebPageTask();
            task.execute(values);
        }
        else{
            p1Series.appendData(new DataPoint(counter, p1), true, 100);
            p2Series.appendData(new DataPoint(counter, p2), true, 100);
            vSeries.appendData(new DataPoint(counter, ref), true, 100);
        }

    }

    private void logData() {
        max =(int) myDb.getSize();
        runOnUiThread(new Runnable() {
            public void run() {
                cdd = new LoadingDialog(SICActivity.this);
                cdd.setCancelable(false);
                cdd.show();
                cdd.setMax(myDb.getSize());
            }
        });

        DBtoCSV task = new DBtoCSV();
        task.execute("something");

    }

    /**
     * Method that access the internal storage on the tablet in order to access log files
     */
    private void rescanSD(final File toScan) {

        MediaScannerConnection.MediaScannerConnectionClient MSCC = new MediaScannerConnection.MediaScannerConnectionClient() {

            public void onScanCompleted(String path, Uri uri) {
                Log.i(TAG, "Media Scan Completed.");
            }

            public void onMediaScannerConnected() {
                MSC.scanFile(toScan.getAbsolutePath(), null);
                Log.i(TAG, "Media Scanner Connected.");
            }
        };
        MSC = new MediaScannerConnection(getApplicationContext(), MSCC);
        MSC.connect();
    }

    public static short twoBytesToShort(byte b1, byte b2) {
        return (short) ((b1 << 8) | (b2 & 0xFF));
    }

    private void p1Graph() {

        p1Series = new LineGraphSeries();
        p1Series.setColor(Color.RED);

        //p1graph.setManualYAxisBounds(127, -127);
        p1graph.addSeries(p1Series); // data
        p1graph.getViewport().setYAxisBoundsManual(true);
        p1graph.getViewport().setMaxY(4096);
        p1graph.getViewport().setMinY(-1);
        p1graph.getViewport().setScalable(false);
        p1graph.getViewport().setScrollable(false);

        p1graph.setTitle("Upper Sensor Leg");
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

        p2graph.setTitle("Lower Sensor Leg");
        p2graph.setTitleColor(R.color.PMDBlue);
        p2graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        p2graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.p2Layout);
        layout2.addView(p2graph);

        //////////////////////////////////////
        vSeries = new LineGraphSeries();
        vSeries.setColor(Color.rgb(0, 156, 2));

        //p1graph.setManualYAxisBounds(127, -127);
        vgraph.addSeries(vSeries); // data
        vgraph.getViewport().setYAxisBoundsManual(true);
        vgraph.getViewport().setMaxY(4096);
        vgraph.getViewport().setMinY(-1);
        vgraph.getViewport().setScalable(false);
        vgraph.getViewport().setScrollable(false);

        vgraph.setTitle("Reference Voltage");
        vgraph.setTitleColor(R.color.PMDBlue);
        vgraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        vgraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        RelativeLayout layout3 = (RelativeLayout) findViewById(R.id.vRefLayout);
        layout3.addView(vgraph);


    }



}

