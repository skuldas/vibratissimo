package de.hieder.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements Controller.ControllerCallback {

    final static String TAG = "MainActivity";
    private final static int REQUEST_ENABLE_BT = 1;

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    EditText etHost;

    Button btnServerConnect;
    Button btnVibroConnect;

    TextView tvServerStatus;
    TextView tvVibroStatus;

    Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etHost = findViewById(R.id.et_host);

        btnServerConnect = findViewById(R.id.btn_server_connect);
        btnVibroConnect = findViewById(R.id.btn_vibro_connect);

        tvServerStatus = findViewById(R.id.tv_server_status);
        tvVibroStatus = findViewById(R.id.tv_vibro_status);

        btnServerConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(btnServerConnect.getText().toString().equals("CONNECT")){
                    controller.enableNetwork(true,etHost.getText().toString());
                }else{
                    controller.enableNetwork(false,null);
                }
            }
        });

        btnVibroConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(btnVibroConnect.getText().toString().equals("CONNECT")){
                    controller.enableVibro(true);
                }else{
                    controller.enableVibro(false);
                }
            }
        });

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Le klappt nicht!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Der App wurden noch nicht alle Rechte gegeben!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1", Toast.LENGTH_LONG).show();
        }

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        onServerConnection(false);

        controller = new Controller(MainActivity.this, bluetoothAdapter, this);
        controller.enableNetwork(true,"hieder.de:443/vibro");
        controller.enableVibro(true);

    }

    void onServerConnection(final boolean state){

        runOnUiThread(new Runnable() {
            public void run() {
                if(state){
                    tvServerStatus.setText("Server status : ONLINE");
                    etHost.setEnabled(false);
                    btnServerConnect.setText("DISCONNECT");

                    tvVibroStatus.setVisibility(View.VISIBLE);
                    btnVibroConnect.setVisibility(View.VISIBLE);
                }else{
                    tvServerStatus.setText("Server status : OFFLINE");
                    etHost.setEnabled(true);
                    btnServerConnect.setText("CONNECT");

                    tvVibroStatus.setVisibility(View.INVISIBLE);
                    btnVibroConnect.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    void onVibroConnection(final boolean state){
        runOnUiThread(new Runnable() {
            public void run() {
                if(state){
                    tvVibroStatus.setText("Vibro status : ONLINE");
                    btnVibroConnect.setText("DISCONNECT");

                }else{
                    tvVibroStatus.setText("Vibro status : OFFLINE");
                    btnVibroConnect.setText("CONNECT");
                }
            }
        });
    }


    @Override
    public void changeNetworkConnection(Controller.ConnectionState state) {

        if(state == Controller.ConnectionState.CONNECTED){

            onServerConnection(true);

        }else if(state == Controller.ConnectionState.DISCONNECTED){
            onServerConnection(false);

        }
    }

    @Override
    public void networkConnectionFail(final Throwable t) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this,t.toString(),Toast.LENGTH_LONG).show();
            }
        });
        onServerConnection(false);

    }

    @Override
    public void changeVibroConnection(Controller.ConnectionState state) {
        Log.i(TAG, "changeVibroConnection( " + state + " )");
        if(state == Controller.ConnectionState.CONNECTED){
            onVibroConnection(true);
        }else{
            onVibroConnection(false);
        }
    }

    @Override
    public void vibroConnectionFail(final Throwable t) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this,t.toString(),Toast.LENGTH_LONG).show();
            }
        });
        Log.i(TAG, "vibroConnectionFail", t);
        onVibroConnection(false);
    }
}
