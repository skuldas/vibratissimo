package de.hieder.test;

import android.util.Log;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.hieder.test.entity.ServerMsg;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class NetworkService extends Thread{

    final static String TAG = "NetworkService";

    public static interface WebsocketConnectCallback{
        void connected();
        void disconnected();
        void fail(Throwable t);
        void onMessage(String msg);
    }

    String host;

    OkHttpClient client = new OkHttpClient();
    WebSocket ws;

    boolean startConnect=false;
    boolean connecting = false;
    boolean connected =false;

    WebsocketConnectCallback connectCallback;

    public NetworkService(){

    }

    @Override
    public void run() {

        while (isInterrupted()==false){

            if(startConnect && !connected && !connecting){
                Log.i(TAG,"startConnect && !connected && !connecting");
                connecting = true;
                connect();
            }else{
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        closeConnection();
    }

    public void connectToServer(String host,WebsocketConnectCallback callback){
        Log.i(TAG,"_connectToServer( "+host+")");
        if(!startConnect){
            this.host = host;
            this.connectCallback = callback;
            startConnect = true;
        }
    }

    public void closeConnection() {
        Log.i(TAG,"closeConnection()");
        ws.close(1000,"");
    }

    private void connect(){
        Log.i(TAG,"connect()");
        client = new OkHttpClient();
        System.out.println("Try to connect to webserver");

        try{
            Request request = new Request.Builder().url("wss://"+host+"/socket/smartphone").build();

            ws = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    Log.i(TAG,"connect() - onClosed");
                    startConnect=false;
                    connected = false;
                    connecting=false;
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    Log.i(TAG,"connect() - onClosing");
                    startConnect=false;
                    connected = false;
                    connecting=false;
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                    Log.i(TAG,"connect() - onFailure");
                    if(startConnect){

                        startConnect=false;
                        connected = false;
                        connecting=false;
                        connectCallback.fail(t);
                    }
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    Log.i(TAG,"connect() - onMessage("+text+")");
                    connectCallback.onMessage(text);
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                    Log.i(TAG,"connect() - onMessage("+bytes+")");
                    connectCallback.onMessage(bytes.toString());
                }

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    Log.i(TAG,"connect() - onOpen");
                    if(startConnect){
                        startConnect=false;
                        connected = true;
                        connecting=false;
                        connectCallback.connected();
                    }
                }
            });
            client.dispatcher().executorService().shutdown();
        }catch(Exception e){
            connectCallback.fail(e.fillInStackTrace());
        }

    }

    public boolean send(String msg){
        if(connected) {
            ws.send(msg);
            return true;
        }
        return false;
    }

    public boolean sendBluetoothState(boolean state){

        Gson gson = new Gson();
        if(connected){
            if(state){
                send(gson.toJson(new ServerMsg("BLUETOOTH","true")));
            }else{
                send(gson.toJson(new ServerMsg("BLUETOOTH","false")));
            }
        }
        return false;
    }

    public boolean isConnected() {
        return connected;
    }
}
