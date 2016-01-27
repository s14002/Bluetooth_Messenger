package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class BTCommunicator extends AppCompatActivity {

    public BTCommunicator(MainActivity mainActivity, Handler myHandler, BluetoothAdapter defaultAdapter) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btcommunicator);
    }
}
