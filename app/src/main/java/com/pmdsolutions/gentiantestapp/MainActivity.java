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
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pmdsolutions.gentiantestapp.support.CustomScannerActivity;
import com.pmdsolutions.gentiantestapp.support.DeviceListAdapter;
import com.pmdsolutions.gentiantestapp.support.Devices;
import com.pmdsolutions.gentiantestapp.support.ScanModeDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.UUID;



public class MainActivity extends Activity implements View.OnClickListener, BluetoothAdapter.LeScanCallback {

    private static final UUID MAIN_SERVICE = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
    private static final UUID DATA_CHAR = UUID.fromString("0000FFB1-0000-1000-8000-00805f9b34fb");

    private static final UUID REMAME_SERVICE = UUID.fromString("0000FFF0-0000-1000-8000-00805f9b34fb");
    private static final UUID REMAME_CHAR = UUID.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");

    private static final UUID SILENCE_CHAR = UUID.fromString("0000fff5-0000-1000-8000-00805f9b34fb");

    private static final UUID CONFIG_CHAR = UUID.fromString("0000FFB2-0000-1000-8000-00805f9b34fb");

    private static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID FINISH  = UUID.fromString("0000FFF4-0000-1000-8000-00805f9b34fb");

    private final UUID SECURITY_KEY             = UUID.fromString("0000FFFD-0000-1000-8000-00805f9b34fb");

    private final UUID BATTERY_SERVICE_UUID	 	 = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private final UUID BATTERY_CHAR_UUID		 = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter adapter;
    public static String MaintCycle = "10000";
    private ArrayList<Devices> deviceList = new ArrayList();
    private Handler timeoutHandler;
    private static Handler stepHandler;
    private DeviceListAdapter listAdapter;
    private ListView listView;
    boolean timedout = false;
    private static ProgressDialog progressDialog;
    private SharedPreferences read_prefs;
    static int commsComplete;
    private String MaintDevice;
    private Intent startingIntent;
    public boolean testing = false;
    private int ledLogs[] = new int[3];
    private int batteryLevel;
    private int data;
    public boolean maintRunning = true;
    private ArrayList<Integer> errorCodes;
    private String batteryCharge;
    private boolean batteryOK = false;
    private boolean errorsOK = false;
    private boolean dataOK = false;
    private boolean redOK = true;
    private boolean greenOK = true;
    private boolean blueOK = true;
    private boolean alarmOK = true;
    final MediaPlayer mp = new MediaPlayer();
    public boolean function;
    private boolean test = false;

    private ArrayAdapter<String> arrayAdapter;

    private ProgressDialog mProgress;

    BluetoothGatt gatt2;

    private String TAG = "Main Activity";

    private BluetoothDevice currentDevice;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevices;

    private BluetoothGatt mConnectedGatt;

    private static LoginHandler  mHandler = null;

    private TextView bomNum, firmNum, blueNum;

    private boolean connected = true;

    public static MainActivity instance;

    /*
     * We have a Handler to process event results on the main thread
     */
    private static final int MSG_HUMIDITY = 101;
    private static final int MSG_PRESSURE = 102;
    private static final int MSG_PRESSURE_CAL = 103;
    private static final int MSG_PROGRESS = 201;
    private static final int MSG_DISMISS = 202;
    private static final int MSG_CLEAR = 301;

    private final Runnable scanTimeout = new Runnable() {
        public void run() {
            getDevice();
            //getAdapter().stopLeScan(renameReceiver);

        }
    };

    private final  Runnable period = new Runnable() {
        public void run() {
            //nextStep();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;
        progressDialog = new ProgressDialog(this);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothAdapter.enable();
        mDevices = new ArrayList<BluetoothDevice>();
        attachOnClickListeners();

        bomNum = (TextView) findViewById(R.id.bomNum);
        firmNum = (TextView) findViewById(R.id.firmwareNum);
        blueNum = (TextView) findViewById(R.id.bluetoothNum);

        bomNum.setText(GlobalValues.Version.BOMNUMBER);
        firmNum.setText(GlobalValues.Version.FIRMWARE);
        blueNum.setText(GlobalValues.Version.BLUETOOTH);
        connected = true;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i("Scanned! ", "New LE Device: " + device.getName() + " @ " + rssi);
        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */
        if (device.getName() == null){
            Log.wtf(TAG, "Null Name!");
        }
        else if (!mDevices.contains(device)){
            if (device.getName()!=null){
                arrayAdapter.add(device.getName());
            }
            mDevices.add(device);
            arrayAdapter.notifyDataSetChanged();
        }


        //Update the overflow menu
        invalidateOptionsMenu();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void attachOnClickListeners(){
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.mainLayout);
        ArrayList<View> touchables = rl.getTouchables();
        //sets each touchable item in the layout's on click to itself (see onClick())
        for(View touchable : touchables)
        {
            if(touchable instanceof Button || touchable instanceof ImageButton){
                touchable.setOnClickListener(this);
            }
        }

    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnIdentify:
                mBluetoothAdapter.enable();
                launchIdentify();
                break;

            case R.id.btnTestUnit:
                if (mBluetoothAdapter.isEnabled()){
                launchTest();
            }
                else{
                    mBluetoothAdapter.enable();
                }


                break;

            default:
                break;
        }
    }

    public void launchTest() {
        ScanModeDialog smd = new ScanModeDialog(MainActivity.this);
        smd.setCancelable(true);
        smd.show();
        test = true;
    }

    public void launchIdentify() {

        showDeviceDialog();mDevices.clear();
        startScan(1);
//        ScanModeDialog smd = new ScanModeDialog(MainActivity.this);
//        smd.setCancelable(true);
//        smd.show();

        test = false;
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        //setProgressBarIndeterminateVisibility(false);
        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }


    }

    private void showDeviceDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select One Name:-");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        //arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        Log.wtf("Something", mDevices.size() + "");
        for (int i=0; i < mDevices.size(); i++) {
            if (mDevices.get(i) != null)
            arrayAdapter.add(mDevices.get(i).getName());
        }


        builderSingle.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentDevice = mDevices.get(which);
                        stopScan();

                        //Display progress UI
                        //mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + currentDevice.getName() + "..."));

                        String strName = arrayAdapter.getItem(which);
                        if (test) {
                            //String SICDeviceName = data.getStringExtra("DEVICE_NAME");
                            Intent scanIntent = new Intent(MainActivity.this, SICActivity.class);
                            scanIntent.putExtra("DEVICE", strName);
                            scanIntent.putExtra("MODE", 1);
                            startActivity(scanIntent);
                        } else {
                            mConnectedGatt = currentDevice.connectGatt(MainActivity.this, false, mGattCallback);
                            AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                            builderInner.setTitle(strName);
                            builderInner.setMessage("Activating Blue LED...");
                            builderInner.setPositiveButton("Finish",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            BluetoothGattCharacteristic characteristic;

                                            Log.d(TAG, "Disabling Sensors");
                                            characteristic = gatt2.getService(MAIN_SERVICE)
                                                    .getCharacteristic(FINISH);
                                            byte finisher = 0x01;
                                            characteristic.setValue(new byte[]{finisher});

                                            gatt2.writeCharacteristic(characteristic);
                                            connected = false;
                                        }
                                    });
                            builderInner.show();
                        }
                    }
                });
        builderSingle.show();
    }

    @Override
    protected void onResume() {
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothAdapter.enable();
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothAdapter.enable();
        super.onPostResume();
    }

    private void startScan(int mode) {
        deviceList.clear();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothAdapter.enable();
        timeoutHandler = new Handler();
        if (mode == 1){

        }
        else{
            timeoutHandler.postDelayed(mStopRunnable, 6000);
        }
        mBluetoothAdapter.startLeScan(this);

    }

    public void getDevice() {

        if (mDevices !=null){

            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {

                    listAdapter.setSelectedIndex(pos);
                    MaintDevice = deviceList.get(pos).getName();
                    //tMaint(MaintDevice);
                    commsComplete = 0;

                }
            });
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }
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
                            .getCharacteristic(REMAME_CHAR);
                    //characteristic.setValue(preparePacket());
                    break;
                case 1:
                    Log.d(TAG, "Enabling pressure cal");
                    characteristic = gatt.getService(MAIN_SERVICE)
                            .getCharacteristic(CONFIG_CHAR);
                    characteristic.setValue(new byte[] {0x02});
                    break;


                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

            gatt.writeCharacteristic(characteristic);
        }

//        private byte[] preparePacket() {
//
//            byte[] pack = new byte[20];
//
//            int packetLength = deviceName.length();
//            //Thresholds
//            //0 = upper
//            //1 = lower
//            //Pulled from MainActivity because BLEService does not extend Activity and therefore
//            //has no access to shared preferences.
//            int[] thresholds = {61, 5};
//
//            pack[0] = (byte) packetLength;
//            for(int i = 1; i <= deviceName.length(); i++){
//                pack[i] = (byte) deviceName.charAt(i - 1);
//            }
//
//            int j = deviceName.length()+1;
//            pack[j] = (byte) thresholds[0];
//            pack[j+1] = (byte) thresholds[1];
//
//            for(int y = (deviceName.length() + 3); y<pack.length; y++){
//                pack[y] = (byte)0;
//            }
//            return pack;
//        }
//


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
                case 0:
                    Log.d(TAG, "Set notify pressure cal");
                    characteristic = gatt.getService(MAIN_SERVICE)
                            .getCharacteristic(DATA_CHAR);
                    break;

                default:
                    mHandler.sendEmptyMessage(MSG_DISMISS);
                    Log.i(TAG, "All Sensors Enabled");
                    return;
            }

//            bytesp1 = new ArrayList<>();
//            bytesp2 = new ArrayList<>();
//            bytesv = new ArrayList<>();

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
                gatt.discoverServices();
            //    mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Activating Blue LED..."));
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

                gatt.close();
                //runOnUiThread(reScan);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "Services Discovered: " + status);
         //   mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Enabling Sensors..."));
            /*
             * With services discovered, we are going to reset our state machine and start
             * working through the sensors we need to enable
             */
            reset();
            writeSecurity();
            //disableSensors(gatt);
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

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //For each read, pass the data up to the UI thread to update the display
            if (DATA_CHAR.equals(characteristic.getUuid())) {
                mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));

                setNotifyNextSensor(gatt);
            }
            else if (BATTERY_CHAR_UUID.equals(characteristic.getUuid())){

//                final String batteryCharge = Byte.toString( characteristic.getValue()[0]);
//                battCharge = batteryCharge;
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        battLevel.setText("Lobe Battery: " +batteryCharge+ "%");
//                    }
//                });
            }
            //After reading the initial value, next we enable notifications

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //After writing the enable flag, next we read the initial value
            //setNotifyNextSensor(gatt);
            if(characteristic.getUuid().toString().equalsIgnoreCase(String.valueOf(SECURITY_KEY))){
                disableSensors(gatt);
            }
            if (!connected){
                mConnectedGatt.close();
                connected = true;
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
//                if ((sicAct.stillRunning)) {
//                    mHandler.sendMessage(Message.obtain(null, MSG_HUMIDITY, characteristic));
//                }
//                else{
//                    disableSensors(gatt);                }
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
            gatt2 = gatt;
            Log.d(TAG, "Disabling Sensors");
            characteristic = gatt.getService(MAIN_SERVICE)
                    .getCharacteristic(FINISH);
            byte finisher = 0x08;
            characteristic.setValue(new byte[] {finisher});

            gatt.writeCharacteristic(characteristic);
            //sicAct.stillRunning = true;
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

    public void launchScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CustomScannerActivity.class);
        integrator.setPrompt("Scan a barcode");
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    public void launchBluetooth() {
        mBluetoothAdapter.enable();
        mDevices.clear();
        showDeviceDialog();
//        progressDialog = ProgressDialog.show(this, "Scanning...",
//                "Please Wait", true);

        startScan(1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                if (test) {
                    Intent scanIntent = new Intent(MainActivity.this, SICActivity.class);
                    scanIntent.putExtra("DEVICE", contents);
                    scanIntent.putExtra("MODE", 1);
                    startActivity(scanIntent);
                }
                else {
                    mConnectedGatt = currentDevice.connectGatt(MainActivity.this, false, mGattCallback);
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                    builderInner.setTitle(contents);
                    builderInner.setMessage("Activating Blue LED...");
                    builderInner.setPositiveButton("Finish",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    BluetoothGattCharacteristic characteristic;

                                    Log.d(TAG, "Disabling Sensors");
                                    characteristic = gatt2.getService(REMAME_SERVICE)
                                            .getCharacteristic(FINISH);
                                    byte finisher = 0x00;
                                    characteristic.setValue(new byte[] {finisher});

                                    gatt2.writeCharacteristic(characteristic);
                                    connected = false;
                                }
                            });
                    builderInner.show();
                }
//                Toast.makeText(getApplicationContext(), contents, Toast.LENGTH_LONG).show();
//                Intent resultIntent = new Intent(MainActivity.this, ResultActivity.class);
//                resultIntent.putExtra("DEVICE_NAME", contents);
//                startActivity(resultIntent);

            } else {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class LoginHandler extends Handler {

        private WeakReference<MainActivity> mTarget;

        LoginHandler(MainActivity target) {
            mTarget = new WeakReference<MainActivity>(target);
        }

        public void setTarget(MainActivity target) {
            mTarget = new WeakReference<MainActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            // process incoming messages here
            MainActivity activity = mTarget.get();

            BluetoothGattCharacteristic characteristic;

            switch (msg.what) {


                case MSG_HUMIDITY:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                 //       Log.w(TAG, "Error obtaining humidity value");
                        return;
                    }
                   // updateHumidityValues(characteristic);
                  //  activity.updatePressureValue(characteristic);
                    break;
                case MSG_PRESSURE:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                   //     Log.w(TAG, "Error obtaining pressure value");
                        return;
                    }
                   // activity.updatePressureValue(characteristic);
                    break;
                case MSG_PRESSURE_CAL:
                    characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if (characteristic.getValue() == null) {
                   //    Log.w(TAG, "Error obtaining cal value");
                        return;
                    }
                   // activity.updatePressureCals(characteristic);
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
                    //clearDisplayValues();
                    break;
            }


        }
    }
}
