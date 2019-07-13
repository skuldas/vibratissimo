package de.hieder.test;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import de.hieder.test.entity.ServerMsg;

public class Controller {

    final static String TAG = "Controller";

    public enum ConnectionState{
        CONNECTED,
        DISCONNECTED
    }
    public interface ControllerCallback{
        void changeNetworkConnection(ConnectionState state);
        void networkConnectionFail(Throwable t);

        void changeVibroConnection(ConnectionState state);
        void vibroConnectionFail(Throwable t);
    }

    Gson gson = new Gson();
    ControllerCallback callback;
    NetworkService networkService;
    VibroService vibroService;

    public Controller(Context context, BluetoothAdapter bluetoothAdapter,ControllerCallback callback){
        this.callback = callback;
        networkService = new NetworkService();
        networkService.start();

        vibroService = new VibroService(context,bluetoothAdapter);
        vibroService.start();
    }

    public void enableNetwork(boolean enable,String host){

        if(enable){
            Log.i(TAG,"enableNetwork( TRUE )");
            networkService.connectToServer(host,new NetworkService.WebsocketConnectCallback() {
                @Override
                public void connected() {
                    Log.i(TAG,"enableNetwork - CONNECTED");
                    callback.changeNetworkConnection(ConnectionState.CONNECTED);
                }

                @Override
                public void disconnected() {
                    Log.i(TAG,"enableNetwork - DISCONNECTED");
                    callback.changeNetworkConnection(ConnectionState.DISCONNECTED);
                }

                @Override
                public void fail(Throwable t) {
                    Log.i(TAG,"enableNetwork - FAIL",t);
                    callback.networkConnectionFail(t);
                }

                @Override
                public void onMessage(String msg) {

                    ServerMsg sMsg = gson.fromJson(msg, ServerMsg.class);

                    if(sMsg.getType().equals("STATE_CHECK") && sMsg.getVal().equals("BLUETOOTH")){
                        networkService.sendBluetoothState( vibroService.isConnected() );
                    }
                    else if(sMsg.getType().equals("CONNECT_TO_VIB")){
                        if(sMsg.getVal().equals("true")){
                            vibroService.connect(null);
                        }else{
                            vibroService.disconnect();
                        }
                    }
                    else if(sMsg.getType().equals("CHANGE_VIB_STATE")){
                        if(sMsg.getVal().equals("true")){
                            vibroService.setMode((byte)0x03,(byte)0x80);
                        }else{
                            vibroService.setMode((byte)0x00,(byte)0x80);
                        }
                    }
                    else if(sMsg.getType().equals("CHANGE_VIB_VAL")){
                       float newVal = (float)Integer.parseInt(sMsg.getVal()) * 255.0f / 100;
                        vibroService.setValue((byte)(int)newVal);

                    }

                }
            });
        }else{
            Log.i(TAG,"enableNetwork( FALSE )");

            networkService.closeConnection();
            callback.changeNetworkConnection(ConnectionState.DISCONNECTED);
        }
    }

    public void enableVibro(boolean enable){

        if(enable){
            Log.i(TAG,"enableVibro( TRUE )");
            vibroService.connect(new VibroService.VibroConnectCallback() {
                @Override
                public void connected() {
                    networkService.sendBluetoothState( true );
                    Log.i(TAG,"enableVibro - CONNECTED");
                    callback.changeVibroConnection(ConnectionState.CONNECTED);
                }

                @Override
                public void disconnected() {
                    networkService.sendBluetoothState( false );
                    Log.i(TAG,"enableVibro - DISCONNECTED");
                    callback.changeVibroConnection(ConnectionState.DISCONNECTED);
                }

                @Override
                public void fail(Throwable t) {
                    networkService.sendBluetoothState( false );
                    Log.i(TAG,"enableVibro - FAIL",t);
                    callback.vibroConnectionFail(t);
                }
            });
        }else{
            Log.i(TAG,"enableVibro( FALSE )");
            vibroService.disconnect();
        }

    }

}
