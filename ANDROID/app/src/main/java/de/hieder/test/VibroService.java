package de.hieder.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class VibroService extends Thread {

    final static String TAG = "VibroService";

    public static interface VibroConnectCallback {
        void connected();

        void disconnected();

        void fail(Throwable t);
    }

    BluetoothAdapter bluetoothAdapter;
    Context context;

    HashMap<String, BluetoothGattCharacteristic> characteristicMap = new HashMap<>();

    VibroConnectCallback callback;

    Queue<SendData> queue = new ConcurrentLinkedQueue<SendData>();
    AtomicBoolean lock = new AtomicBoolean(false);

    final long MAX_SCAN_TIME = 10000;
    final long MAX_CONNECT_TIME = 5000;
    final long MAX_DISCOVER_TIME = 5000;

    long lastStageTime = 0;
    boolean forceQuit = false;
    boolean connected = false;
    int stage = 0;
    boolean staging=false;

    BluetoothDevice xDevice = null;
    BluetoothGatt mGatt=null;

    public VibroService(Context context, BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.context = context;

    }

    void xsleep(int len){
        try {
            sleep(len);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {

            if (connected) {
                if (queue.size() != 0 && !lock.get()) {
                    lock.set(true);
                    SendData data = queue.poll();
                    switch (data.getType()) {
                        case SLEEP:
                            try {
                                sleep(data.getSleep());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            lock.set(false);
                        case MODE:
                            characteristicMap.get("00001524-1212-efde-1523-785feabcd123").setValue(data.getData());
                            mGatt.writeCharacteristic(characteristicMap.get("00001524-1212-efde-1523-785feabcd123"));
                            break;
                        case VALUE:
                            characteristicMap.get("00001526-1212-efde-1523-785feabcd123").setValue(data.getData());
                            mGatt.writeCharacteristic(characteristicMap.get("00001526-1212-efde-1523-785feabcd123"));
                            break;
                    }

                } else {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                if (stage == 1) {
                    if(staging == false){
                        lastStageTime = System.currentTimeMillis();
                        Log.i(TAG, "start stage 1");
                        staging = true;
                        _startScan();
                    }else{
                        if(forceQuit || System.currentTimeMillis() > (lastStageTime+MAX_SCAN_TIME)){
                            bluetoothAdapter.stopLeScan(scanCallback);
                            connected = false;
                            stage = 0;
                            staging=false;
                            xDevice = null;
                            mGatt=null;
                            callback.fail(new Throwable("MAX_SCAN_TIME or forceQuit problem"));
                            forceQuit = false;
                        }else{
                            xsleep(10);
                        }
                    }
                }
                else if (stage == 2) {

                    if(staging == false){
                        lastStageTime = System.currentTimeMillis();
                        Log.i(TAG, "start stage 2");
                        staging = true;
                        _connect();
                    }else{
                        if(forceQuit || System.currentTimeMillis() > (lastStageTime+MAX_CONNECT_TIME)){
                            mGatt.disconnect();
                            connected = false;
                            stage = 0;
                            staging=false;
                            xDevice = null;
                            mGatt=null;
                            forceQuit = false;
                            callback.fail(new Throwable("MAX_CONNECT_TIME or forceQuit problem"));
                        }else{
                            xsleep(10);
                        }
                    }
                }
                else if (stage == 3 && staging == false) {
                    if(staging == false){
                        Log.i(TAG, "start stage 3");
                        staging = true;
                        mGatt.discoverServices();
                    }else{
                        if(forceQuit || System.currentTimeMillis() > (lastStageTime+MAX_DISCOVER_TIME)){
                            lastStageTime = System.currentTimeMillis();
                            mGatt.disconnect();
                            connected = false;
                            stage = 0;
                            staging=false;
                            xDevice = null;
                            mGatt=null;
                            forceQuit = false;
                            callback.fail(new Throwable("MAX_DISCOVER_TIME or forceQuit problem"));
                        }else{
                            xsleep(10);
                        }
                    }
                }else{
                    xsleep(10);
                }
            }
        }
        disconnect();
    }

    private void _startScan() {
        bluetoothAdapter.startLeScan(scanCallback);
    }

    private void _connect() {
        mGatt = xDevice.connectGatt(context, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.i(TAG, "_connectToServer() - STATE_CONNECTED");
                        stage   = 3;
                        staging = false;
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        Log.i(TAG, "_connectToServer() - STATE_CONNECTING");
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.i(TAG, "_connectToServer() - STATE_DISCONNECTED");

                        connected = false;
                        stage = 0;
                        staging=false;
                        xDevice = null;
                        mGatt=null;

                        callback.disconnected();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        Log.i(TAG, "_connectToServer() - STATE_DISCONNECTING");
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.i(TAG, "onServicesDiscovered()");
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService item : services) {

                    if (item.getUuid().toString().endsWith("123")) {
                        for (BluetoothGattCharacteristic item2 : item.getCharacteristics()) {
                            characteristicMap.put(item2.getUuid().toString(), item2);
                        }
                        break;
                    }
                }

                connected = true;
                stage = 0;
                staging=false;

                callback.connected();
                Log.i(TAG, "FULL CONNECTED!!!!!");

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                lock.set(false);
                Log.i(TAG, "WRITE");
            }
        });
    }

    public boolean connect(VibroConnectCallback callback) {
        if(connected == false && stage == 0 && staging == false){

            if(callback != null){
                this.callback = callback;
            }
            stage = 1;
            return true;
        }
        return false;
    }

    public boolean disconnect() {
        if(connected){
            mGatt.disconnect();
        }else{
            forceQuit = true;
        }
        return true;
    }

    public void setMode(byte b1, byte b2) {
        queue.add(new SendData(SendData.SendDataType.MODE, new byte[]{b1, b2}));
    }

    public void setValue(byte b) {
        queue.add(new SendData(SendData.SendDataType.VALUE, new byte[]{b, b, b}));
    }

    public void setValueSleep(int sleep) {
        queue.add(new SendData(SendData.SendDataType.SLEEP, sleep));
    }

    public boolean isConnected(){
        return connected;
    }

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            Log.i(TAG, "startScan() - " + device);
            if (device.toString().equals("DE:B0:D8:6A:CB:5F") && xDevice == null) {
                xDevice = device;
                stage   = 2;
                staging = false;

                Log.i(TAG, "found DE:B0:D8:6A:CB:5F !!!!!!!!!!!!!!!!");
                Log.i(TAG, "Stop Scanning");
                bluetoothAdapter.stopLeScan(this);
            }
        }
    };

}
