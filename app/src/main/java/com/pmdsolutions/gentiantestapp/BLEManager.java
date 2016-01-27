package com.pmdsolutions.gentiantestapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * This class is responsible for managing all the interactions with the device.
 * This is achieved by implementing threads.
 * @author Daniel Hosford
 *
 */
public class BLEManager {

    private final String TAG = BLEManager.class.getSimpleName();

    private BLEService service;
    private BluetoothDevice device;

    public boolean dataThreadCompleted = false;
    private boolean renameThreadCompleted = false;

    private String patientMRN;

    private static BLEManager instance;

    private final long THREAD_WAIT = 5000;
    private String name;

    public boolean silence = false;
    private boolean connected;

    /**
     * Constructor
     * @param context "getApplicationContext()"
     */
    public BLEManager(Context context){
        instance = this;
        service = new BLEService(context);
    }

    //===================================================================================//
    //+++++++++++++++++++++++++++++++++++++METHODS+++++++++++++++++++++++++++++++++++++++//
    //===================================================================================//

    public void setDevice(BluetoothDevice deviceIn){
        this.device = deviceIn;
        this.name = deviceIn.getName();
    }

    public String getName(){
      return "Something";
    }

    public void connectDevice(int i) {
        service.getInstance().connectDevice(device, i);
    }

    public BLEManager getInstance(){
        return instance;
    }

    public void writeThresholds(int upperThreshold, int lowerThreshold) {
        service.getInstance().writeThresholds(upperThreshold, lowerThreshold);
    }

    public void renameDevice(BluetoothDevice renameDevice, String renameMRN) {
        service.getInstance().renameDevice(renameDevice.getName(), renameMRN);
    }

    public void disconnect(int i) {
        service.getInstance().disconnect(i);
    }

    public void writeStatus(boolean pause, byte statusString) {
        service.writeStatus(pause, statusString);
    }

    public void writeAlert() {
        service.writeAlert();
    }


    public void sendMaintChar(int i) {
        service.sendMaintChar(i);
    }

    public void startStreaming() {
        service.startStreaming();
    }

    public boolean isConnected() {
        connected = service.getConnectionState();
        return connected;
    }

    public void activateLED(int i) {
        service.getInstance().activateLED(i);
    }
}
