package jp.ac.it_college.std.s14002.android.bluetoothmessenger;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CONNECT_DEVICE = 1000;
    private static final int REQUEST_ENABLE_BT = 2000;
    static String TAG = "Menu";
    private BluetoothAdapter bluetoothAdapter = null;
    private BTCommunicator myBTCommunicator = null;
    private ProgressDialog connectingProgressDialog;
    private boolean connected = false;
    private boolean bt_error_pending = false;
    private Handler btcHandler;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    /*
                    newDevice = data.getExtras().getBoolean(DeviceListActivity.PAIRING);
                    if (newDevice == true) {
                        enDiscoverable();
                    }
                    */
                    startBTCommunicator(address);
                }
                break;

            case REQUEST_ENABLE_BT:
                switch (resultCode) {

                    //BluetoothがONにされた場合の処理
                    case Activity.RESULT_OK:
//                        selectDevices();
                        Toast.makeText(this, R.string.bt_is_enabled, Toast.LENGTH_LONG).show();
                        break;

                    //BluetoothがOFFにされた場合の処理
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, R.string.bt_needs_to_be_enabled, Toast.LENGTH_LONG).show();
                        finish();
                        break;

                    default:
                        Toast.makeText(this, R.string.problem_at_connecting, Toast.LENGTH_LONG).show();
                        finish();
                        break;
                }
        }
    }

    private void startBTCommunicator(String address) {
        connectingProgressDialog = ProgressDialog.show(this, "", getResources().getString(
                R.string.connecting_please_wait), true);

        if (myBTCommunicator == null) {
            createBTCommunicator();
        }

       /*
       switch ((myBTCommunicator).getState()) {
            case NEW:
                myBTCommunicator.setMacAddress(address);
                myBTCommunicator.start();
                break;
            default:
                connected = false;
                myBTCommunicator = null;
                createBTCommunicator();
                myBTCommunicator.setMacAddress(address);
                myBTCommunicator.start();
                break;
        }*/
        updateButtonAndMenu();
    }

    private void updateButtonAndMenu() {

    }

   /*
    final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.getData().getInt("message")) {
                case BTCommunicator.STATE_CONNECTED:
                    connected = true;
                    connectingProgressDialog.dismiss();
                    updateButtonAndMenu();
                    showToastLong(getResources().getString(R.string.connected));
                    showPicture();
                    break;
                case BTCommunicator.STATE_CONNECTERROR:
                    connectingProgressDialog.dismiss();
                    break;

                case  BTCommunicator.CONNECT_RECCEIVEERORR:
                case BTCommunicator.STATE_SENDERROR:
                    destoryBTCommunicator();

                    if (bt_error_pending == false) {
                        bt_error_pending = true;
                        DialogFragment newFragment = MyAlertFragment.newInstance(
                                R.string.bt_error_dialog_title, R.string.bt_error_dialog_message);
                        newFragment.show(getFragmentManager(), "dialog");
                    }

                    break;

            }
        }
    } ;
*/

    private void createBTCommunicator() {
       /*
        myBTCommunicator = new BTCommunicator(this, myHandler, BluetoothAdapter.getDefaultAdapter());
        btcHandler = myBTCommunicator.getHandler();
        */
    }

    private void enDiscoverable() {
        Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



      /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        /*
        menu.add(0, De          viceListActivity.MENU_SELECT_A, 0, R.string.connection_settings);
        menu.add(0, DeviceListActivity.MENU_SELECT_B, 0, R.string.name_settings);
        menu.add(0, DeviceListActivity.MENU_SELECT_C, 0, R.string.quit);
*/
        return true;

    }

    // TODO

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connection_settings:
                Log.d(TAG, "Select connection.");
                selectDevices();
                break;
            case R.id.name_settings:
                Log.d(TAG, "Select name");
                break;
            case R.id.quit:
                Log.d(TAG, "Select quit");
                // ここをダイアログに変えてはい/いいえの選択をさせたい
                Toast.makeText(this, R.string.quit_message, Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
        return true;
    }

    /*
    private void endTone() {
        if (connected) {
            playTone(1568, 1000);
            playTone(1319, 500);
        }
    }
*/

    /*
    private void updateButtonsAndMenu() {
        if (myMenu == null) return;
        myMenu.removeItem(MENU_TOGGLE_CONNECT);
        if (connected) {
            myMenu.add(0, MENU_TOGGLE_CONNECT, 1, getResources().getString(R.string.disconnect));
        } else {
            myMenu.add(0, MENU_TOGGLE_CONNECT, 1, getResources().getString(R.string.connect));
        }
    }
*/

    @Override
    protected void onStart() {
        super.onStart();
        //BluetoothAdapter取得
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter != null) {
            //Bluetooth対応端末の場合の処理
            Toast.makeText(this, R.string.bt_available, Toast.LENGTH_LONG).show();
        } else {
            //Bluetooth非対応端末の場合の処理
            Toast.makeText(this, R.string.bt_is_not_available, Toast.LENGTH_LONG).show();
            finish();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.bt_needs_to_be_enabled, Toast.LENGTH_LONG).show();
            // OFFだった場合、Bluetooth有効化ダイアログを表示
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//            } else {
            // BluetoothがONだった場合の処理
//                selectDevices();

        }
    }



    private void selectDevices() {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

}

